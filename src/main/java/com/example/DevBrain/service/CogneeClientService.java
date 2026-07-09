package com.example.DevBrain.service;

import com.example.DevBrain.config.CogneeProperties;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import com.example.DevBrain.dto.EnrichmentResponse;
import com.example.DevBrain.dto.ForgetResult;
import com.example.DevBrain.dto.GraphFetchRequest;
import com.example.DevBrain.dto.GraphQueryResult;
import com.example.DevBrain.dto.KnowledgeGraphResponse;
import com.example.DevBrain.dto.RepositoryContext;
import com.example.DevBrain.dto.SearchRequest;
import com.example.DevBrain.dto.SearchResult;
import com.example.DevBrain.dto.SearchType;
import com.example.DevBrain.exception.CogneeUnavailableException;
import com.example.DevBrain.exception.DatasetNotFoundException;
import com.example.DevBrain.exception.HttpServerErrorException;
import com.example.DevBrain.exception.InvalidQuestionException;
import com.example.DevBrain.exception.RateLimitExceededException;
import com.example.DevBrain.exception.TLSHandshakeException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

@Service
public class CogneeClientService {

    private static final Logger log = LoggerFactory.getLogger(CogneeClientService.class);
    private static final List<Map<String, Object>> apiLogs = Collections.synchronizedList(new LinkedList<>());
    private static final ObjectMapper mapper = new ObjectMapper();

    public static List<Map<String, Object>> getApiLogs() {
        return new ArrayList<>(apiLogs);
    }

    public static void clearApiLogs() {
        apiLogs.clear();
    }

    private static void logApiInteraction(String endpoint, String method, Object requestPayload, Object responsePayload, long latencyMs, int status) {
        Map<String, Object> logEntry = new LinkedHashMap<>();
        logEntry.put("timestamp", java.time.LocalDateTime.now().toString());
        logEntry.put("endpoint", endpoint);
        logEntry.put("method", method);
        logEntry.put("requestPayload", serializePayload(requestPayload));
        logEntry.put("responsePayload", serializePayload(responsePayload));
        logEntry.put("latencyMs", latencyMs);
        logEntry.put("status", status);

        synchronized (apiLogs) {
            if (apiLogs.size() >= 50) {
                apiLogs.remove(0);
            }
            apiLogs.add(logEntry);
        }
    }

    private static String serializePayload(Object payload) {
        if (payload == null) return "null";
        if (payload instanceof String s) return sanitizeForLog(s);
        try {
            return sanitizeForLog(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload));
        } catch (Exception e) {
            return sanitizeForLog(String.valueOf(payload));
        }
    }

    private static String sanitizeForLog(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String sanitized = value.replaceAll("[\\r\\n]+", " ").replaceAll("\\s{2,}", " ").trim();
        if (sanitized.length() > 1000) {
            sanitized = sanitized.substring(0, 1000) + "...";
        }
        return sanitized;
    }

    private static final String DATASET_STATUS_ENDPOINT = "/api/v1/datasets/status";
    private static final String COGNIFY_PIPELINE = "cognify_pipeline";
    private static final int STATUS_POLL_MAX_ATTEMPTS = 20;
    private static final long STATUS_POLL_DELAY_MS = 250L;

    private final WebClient webClient;
    private final CogneeProperties properties;
    private final CogneeAvailabilityService availabilityService;
    private final LocalRepositorySearchService localRepositorySearchService;
    private final MeterRegistry meterRegistry;

    public CogneeClientService(@Qualifier("cogneeWebClient") WebClient webClient, CogneeProperties properties,
                               CogneeAvailabilityService availabilityService, LocalRepositorySearchService localRepositorySearchService,
                               MeterRegistry meterRegistry) {
        this.webClient = webClient;
        this.properties = properties;
        this.availabilityService = availabilityService;
        this.localRepositorySearchService = localRepositorySearchService;
        this.meterRegistry = meterRegistry;
    }

    private HttpHeaders buildHeaders(MediaType contentType, MediaType acceptType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(contentType);
        headers.setAccept(List.of(acceptType));
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        if (hasApiKey()) {
            headers.set("X-Api-Key", properties.getApi().getKey());
        }
        return headers;
    }

    private String buildFullUrl(String endpoint) {
        if (!StringUtils.hasText(endpoint)) {
            return "";
        }
        String baseUrl = properties.getApi() != null ? properties.getApi().getBaseUrl() : null;
        if (!StringUtils.hasText(baseUrl)) {
            return endpoint;
        }
        if (endpoint.startsWith("http://") || endpoint.startsWith("https://")) {
            return endpoint;
        }
        String normalizedBase = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String normalizedPath = endpoint.startsWith("/") ? endpoint : "/" + endpoint;
        return normalizedBase + normalizedPath;
    }

    private Map<String, String> maskHeaders(HttpHeaders headers) {
        Map<String, String> masked = new LinkedHashMap<>();
        if (headers == null) {
            return masked;
        }
        headers.forEach((key, values) -> {
            String value = values == null || values.isEmpty() ? "" : String.join(",", values);
            if ("X-Api-Key".equalsIgnoreCase(key)) {
                value = "***MASKED***";
            }
            masked.put(key, value);
        });
        return masked;
    }

    private void logCogneeFailure(String url, String method, HttpHeaders headers, Throwable exception, Integer statusCode, String responseBody) {
        String safeMessage = sanitizeForLog(exception == null ? "null" : exception.getMessage());
        String safeBody = sanitizeForLog(responseBody);
        log.warn(
                "Cognee Cloud request failed. url={}, method={}, statusCode={}, error={}, responseSummary={}",
                url,
                method,
                statusCode,
                safeMessage,
                safeBody
        );
        if (log.isDebugEnabled()) {
            log.debug("Cognee Cloud failure details", exception);
        }
    }

    private void logCogneeRequest(String url, String method, HttpHeaders headers, String contentType, List<String> multipartFields) {
        if (!log.isDebugEnabled()) {
            return;
        }
        log.debug("Cognee Cloud request prepared. url={}, method={}, contentType={}, headers={}, multipartFields={}",
                url,
                method,
                contentType,
                maskHeaders(headers),
                multipartFields);
    }

    private void logCogneeResponse(String url, String method, HttpHeaders headers, int statusCode, String body) {
        if (!log.isDebugEnabled()) {
            return;
        }
        log.debug("Cognee Cloud response received. url={}, method={}, statusCode={}, responseSummary={}",
                url,
                method,
                statusCode,
                sanitizeForLog(body));
    }

    private Integer extractStatusCode(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof WebClientResponseException webClientResponseException) {
                return webClientResponseException.getStatusCode().value();
            }
            current = current.getCause();
        }
        return null;
    }

    private String extractResponseBody(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof WebClientResponseException webClientResponseException) {
                String responseBody = webClientResponseException.getResponseBodyAsString();
                if (StringUtils.hasText(responseBody)) {
                    return responseBody;
                }
            }
            current = current.getCause();
        }
        return throwable == null ? null : throwable.getMessage();
    }

    static String extractErrorMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof WebClientResponseException webClientResponseException) {
                String responseBody = webClientResponseException.getResponseBodyAsString();
                if (StringUtils.hasText(responseBody)) {
                    return responseBody;
                }
            }
            if (StringUtils.hasText(current.getMessage())) {
                return current.getMessage();
            }
            current = current.getCause();
        }
        return null;
    }

    public KnowledgeGraphResponse rememberRepository(String datasetName, String cleanFolderPath) {
        if (!StringUtils.hasText(datasetName)) {
            throw new InvalidQuestionException("Dataset name cannot be blank");
        }

        if (!availabilityService.canAttempt()) {
            localRepositorySearchService.persistMetadata(datasetName, cleanFolderPath);
            KnowledgeGraphResponse fallback = new KnowledgeGraphResponse();
            fallback.setSuccess(false);
            fallback.setDatasetName(datasetName);
            fallback.setStatus("LOCAL_ONLY");
            fallback.setSource("LOCAL");
            fallback.setErrors(List.of("Cognee Cloud unavailable; repository stored locally only."));
            return fallback;
        }

        Path cleanFolder = Path.of(cleanFolderPath);
        meterRegistry.counter("devbrain.uploads.total").increment();
        Timer.Sample sample = Timer.start(meterRegistry);
        log.info("Starting Cognee Cloud ingest for dataset '{}'", datasetName);

        if (!Files.exists(cleanFolder) || !Files.isDirectory(cleanFolder)) {
            throw new IllegalArgumentException("Clean folder does not exist or is not a directory: " + cleanFolder);
        }

        List<Path> files;
        try (Stream<Path> paths = Files.walk(cleanFolder)) {
            files = paths.filter(Files::isRegularFile).toList();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to read repository files for Cognee ingest", e);
        }

        if (files.isEmpty()) {
            throw new IllegalArgumentException("No repository files were found for ingestion");
        }

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("datasetName", datasetName);

        int filesAdded = 0;
        for (Path file : files) {
            try {
                long fileSize = Files.size(file);
                if (fileSize == 0 || fileSize > properties.getMaxFileSize().toBytes()) {
                    continue;
                }

                String relativePath = cleanFolder.relativize(file).toString().replace('\\', '/');
                byte[] content = Files.readAllBytes(file);
                ByteArrayResource resource = new ByteArrayResource(content) {
                    @Override
                    public String getFilename() {
                        return relativePath;
                    }
                };
                builder.part("data", resource);
                filesAdded++;
            } catch (Exception e) {
                log.warn("Skipping file {} during Cognee ingest: {}", file, e.getMessage());
            }
        }

        if (filesAdded == 0) {
            throw new IllegalArgumentException("No ingestible files were found in repository: " + cleanFolder);
        }

        long startTime = System.currentTimeMillis();
        HttpHeaders requestHeaders = buildHeaders(MediaType.MULTIPART_FORM_DATA, MediaType.APPLICATION_JSON);
        String requestUrl = buildFullUrl(properties.getApi().getRememberEndpoint());
        List<String> multipartFields = new ArrayList<>();
        multipartFields.add("datasetName");
        for (Path file : files) {
            try {
                long fileSize = Files.size(file);
                if (fileSize == 0 || fileSize > properties.getMaxFileSize().toBytes()) {
                    continue;
                }
                String relativePath = cleanFolder.relativize(file).toString().replace('\\', '/');
                multipartFields.add("data (filename=" + relativePath + ")");
            } catch (Exception ignored) {
                // ignore for logging
            }
        }
        logCogneeRequest(requestUrl, "POST", requestHeaders,
                requestHeaders.getContentType() == null ? null : requestHeaders.getContentType().toString(),
                multipartFields);
        try {
            String rawResponse = webClient.post()
                    .uri(properties.getApi().getRememberEndpoint())
                    .headers(headers -> headers.addAll(requestHeaders))
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .exchangeToMono(response -> {
                        HttpHeaders responseHeaders = response.headers().asHttpHeaders();
                        int statusCode = response.statusCode().value();
                        return response.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .map(body -> {
                                    logCogneeResponse(requestUrl, "POST", responseHeaders, statusCode, body);
                                    if (response.statusCode().isError()) {
                                        throw new WebClientResponseException(
                                                body,
                                                statusCode,
                                                response.statusCode().toString(),
                                                responseHeaders,
                                                body.getBytes(StandardCharsets.UTF_8),
                                                StandardCharsets.UTF_8);
                                    }
                                    return body;
                                });
                    })
                    .retryWhen(createRetrySpec())
                    .block();

            JsonNode responseBody = StringUtils.hasText(rawResponse) ? mapper.readTree(rawResponse) : null;

            KnowledgeGraphResponse response = new KnowledgeGraphResponse();
            response.setSuccess(true);
            response.setDatasetName(datasetName);
            response.setFilesProcessed(filesAdded);
            response.setBatchesSent(1);
            response.setStatus("COGNEE_READY");
            response.setSource("COGNEE");
            if (responseBody != null) {
                if (responseBody.has("dataset_id") && !responseBody.get("dataset_id").isNull()) {
                    response.setKnowledgeGraphId(responseBody.get("dataset_id").asText());
                } else if (responseBody.has("pipeline_run_id") && !responseBody.get("pipeline_run_id").isNull()) {
                    response.setKnowledgeGraphId(responseBody.get("pipeline_run_id").asText());
                }
            }

            // If the remember API started processing asynchronously, poll dataset status
            if (responseBody != null && responseBody.has("status") && "running".equalsIgnoreCase(responseBody.get("status").asText())) {
                String datasetId = responseBody.has("dataset_id") && !responseBody.get("dataset_id").isNull()
                        ? responseBody.get("dataset_id").asText()
                        : resolveDatasetId(datasetName);
                pollDatasetStatus(response, datasetId);
            }

            localRepositorySearchService.persistMetadata(datasetName, cleanFolderPath);
            availabilityService.recordSuccess();
            long latency = System.currentTimeMillis() - startTime;
            logApiInteraction(properties.getApi().getRememberEndpoint(), "POST", "datasetName=" + datasetName, responseBody, latency, 200);
            return response;
        } catch (Exception e) {
            localRepositorySearchService.persistMetadata(datasetName, cleanFolderPath);
            if (availabilityService.shouldUseFallback(e)) {
                KnowledgeGraphResponse fallback = new KnowledgeGraphResponse();
                fallback.setSuccess(false);
                fallback.setDatasetName(datasetName);
                fallback.setStatus("LOCAL_ONLY");
                fallback.setSource("LOCAL");
                fallback.setErrors(List.of("Cognee Cloud unavailable; repository stored locally only."));
                long latency = System.currentTimeMillis() - startTime;
                logApiInteraction(properties.getApi().getRememberEndpoint(), "POST", "datasetName=" + datasetName, e.getMessage(), latency, 500);
                return fallback;
            }

            long latency = System.currentTimeMillis() - startTime;
            logApiInteraction(properties.getApi().getRememberEndpoint(), "POST", "datasetName=" + datasetName, e.getMessage(), latency, 500);
            logCogneeFailure(requestUrl, "POST", requestHeaders, e, extractStatusCode(e), extractResponseBody(e));
            throw mapAndThrowStructuredException(properties.getApi().getRememberEndpoint(), "POST", e);
        }
    }

    public SearchResult searchRepository(String question, String datasetName) {
        if (!StringUtils.hasText(question)) {
            throw new InvalidQuestionException("Search question cannot be blank");
        }
        if (!StringUtils.hasText(datasetName)) {
            throw new DatasetNotFoundException("Dataset name cannot be blank");
        }

        if (availabilityService.isFallbackActive()) {
            return localRepositorySearchService.search(datasetName, question);
        }

        // Ensure dataset processing completed before executing a search
        String datasetId = resolveDatasetId(datasetName);
        if (!StringUtils.hasText(datasetId)) {
            return SearchResult.builder()
                .answer("")
                .contexts(Collections.emptyList())
                .processing(true)
                .status("processing")
                .source("COGNEE")
                .build();
        }

        String dsStatus = getDatasetStatusById(datasetId);
        if (dsStatus == null || dsStatus.equalsIgnoreCase("pending") || dsStatus.equalsIgnoreCase("running") || dsStatus.equalsIgnoreCase("processing")) {
            return SearchResult.builder()
                .answer("")
                .contexts(Collections.emptyList())
                .processing(true)
                .status("processing")
                .build();
        }
        if (dsStatus.equalsIgnoreCase("failed")) {
            return SearchResult.builder()
                .answer("")
                .contexts(Collections.emptyList())
                .processing(false)
                .status("failed")
                .source("COGNEE")
                .build();
        }

        SearchRequest request = SearchRequest.builder()
            .query(question)
            .datasets(List.of(datasetName))
            .searchType(SearchType.GRAPH_COMPLETION)
            .build();

        long startTime = System.currentTimeMillis();
        HttpHeaders requestHeaders = buildHeaders(MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);
        String requestUrl = buildFullUrl(properties.getApi().getSearchEndpoint());
        try {
            WebClient.RequestBodySpec requestSpec = webClient.post()
                    .uri(properties.getApi().getSearchEndpoint())
                    .headers(headers -> headers.addAll(requestHeaders));

            String responseString = requestSpec
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response -> response.createException().flatMap(Mono::error))
                    .bodyToMono(String.class)
                    .retryWhen(createRetrySpec())
                    .block();
            JsonNode rawResponse = StringUtils.hasText(responseString) ? mapper.readTree(responseString) : null;

            SearchResult result = parseResponse(rawResponse);
            result.setSource("COGNEE");
            result.setStatus(StringUtils.hasText(result.getStatus()) ? result.getStatus() : "COMPLETED");
            availabilityService.recordSuccess();
            long latency = System.currentTimeMillis() - startTime;
            logApiInteraction(properties.getApi().getSearchEndpoint(), "POST", request, rawResponse, latency, 200);
            return result;
        } catch (Exception e) {
            if (availabilityService.shouldUseFallback(e)) {
                return localRepositorySearchService.search(datasetName, question);
            }
            long latency = System.currentTimeMillis() - startTime;
            logApiInteraction(properties.getApi().getSearchEndpoint(), "POST", request, e.getMessage(), latency, 500);
            logCogneeFailure(requestUrl, "POST", requestHeaders, e, extractStatusCode(e), extractResponseBody(e));
            throw mapAndThrowStructuredException(properties.getApi().getSearchEndpoint(), "POST", e);
        }
    }

    private SearchResult parseResponse(JsonNode rawResponse) {
        if (rawResponse == null || rawResponse.isNull()) {
            return SearchResult.builder().answer("").contexts(Collections.emptyList()).build();
        }

        List<RepositoryContext> contexts = new ArrayList<>();
        if (rawResponse.isArray()) {
            for (JsonNode node : rawResponse) {
                String datasetName = node.has("dataset_name") ? node.get("dataset_name").asText() : null;
                JsonNode searchResults = node.has("search_result") ? node.get("search_result") : null;
                if (searchResults != null && searchResults.isArray()) {
                    List<String> results = new ArrayList<>();
                    for (JsonNode resultNode : searchResults) {
                        if (resultNode != null && !resultNode.isNull()) {
                            results.add(resultNode.asText());
                        }
                    }
                    String summary = String.join("\n", results);
                    if (StringUtils.hasText(summary)) {
                        contexts.add(RepositoryContext.builder()
                                .file(datasetName)
                                .summary(summary)
                                .score(1.0)
                                .build());
                    }
                } else if (node.has("content") && !node.get("content").isNull()) {
                    contexts.add(RepositoryContext.builder()
                            .file(datasetName)
                            .summary(node.get("content").asText())
                            .score(1.0)
                            .build());
                }
            }
        }

        String answer = rawResponse.isArray() && rawResponse.size() > 0
                ? rawResponse.get(0).path("search_result").toString()
                : "";

        return SearchResult.builder()
                .answer(answer)
                .contexts(contexts)
                .source("COGNEE")
                .status("COMPLETED")
                .build();
    }

    public EnrichmentResponse improveMemory(String datasetName, String messageId) {
        if (!StringUtils.hasText(datasetName)) {
            throw new DatasetNotFoundException("Dataset name cannot be blank");
        }

        Map<String, Object> request = new LinkedHashMap<>();
        request.put("entry", "Feedback entry for message " + messageId);
        request.put("dataset_name", datasetName);

        long startTime = System.currentTimeMillis();
        HttpHeaders requestHeaders = buildHeaders(MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);
        String requestUrl = buildFullUrl(properties.getApi().getImproveEndpoint());
        try {
            WebClient.RequestBodySpec requestSpec = webClient.post()
                    .uri(properties.getApi().getImproveEndpoint())
                    .headers(headers -> headers.addAll(requestHeaders));

            EnrichmentResponse enrichmentResponse = requestSpec
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, errorResponse -> errorResponse.createException().flatMap(Mono::error))
                    .bodyToMono(EnrichmentResponse.class)
                    .retryWhen(createRetrySpec())
                    .block();

            long latency = System.currentTimeMillis() - startTime;
            logApiInteraction(properties.getApi().getImproveEndpoint(), "POST", request, enrichmentResponse, latency, 200);
            return enrichmentResponse;
        } catch (Exception e) {
            long latency = System.currentTimeMillis() - startTime;
            logApiInteraction(properties.getApi().getImproveEndpoint(), "POST", request, e.getMessage(), latency, 500);
            logCogneeFailure(requestUrl, "POST", requestHeaders, e, extractStatusCode(e), extractResponseBody(e));
            throw mapAndThrowStructuredException(properties.getApi().getImproveEndpoint(), "POST", e);
        }
    }

    public ForgetResult forgetDataset(String datasetName) {
        if (!StringUtils.hasText(datasetName)) {
            throw new DatasetNotFoundException("Dataset name cannot be blank");
        }

        String datasetId = resolveDatasetId(datasetName);
        if (datasetId == null) {
            log.info("Dataset '{}' is not present in Cognee Cloud. Returning success.", datasetName);
            return ForgetResult.builder().success(true).status("NOT_FOUND").build();
        }

        long startTime = System.currentTimeMillis();
        HttpHeaders requestHeaders = buildHeaders(MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);
        String requestUrl = buildFullUrl(properties.getApi().getForgetEndpoint() + "/" + datasetId);
        try {
            WebClient.RequestHeadersSpec<?> requestSpec = webClient.delete()
                    .uri(properties.getApi().getForgetEndpoint() + "/{datasetId}", datasetId)
                    .headers(headers -> headers.addAll(requestHeaders));

            requestSpec.retrieve()
                    .onStatus(HttpStatusCode::isError, response -> response.createException().flatMap(Mono::error))
                    .bodyToMono(Void.class)
                    .retryWhen(createRetrySpec())
                    .block();

            long latency = System.currentTimeMillis() - startTime;
            ForgetResult response = ForgetResult.builder().success(true).status("FORGOTTEN").build();
            logApiInteraction(properties.getApi().getForgetEndpoint() + "/" + datasetId, "DELETE", "datasetName=" + datasetName, response, latency, 200);
            return response;
        } catch (Exception e) {
            long latency = System.currentTimeMillis() - startTime;
            logApiInteraction(properties.getApi().getForgetEndpoint() + "/" + datasetId, "DELETE", "datasetName=" + datasetName, e.getMessage(), latency, 500);
            logCogneeFailure(requestUrl, "DELETE", requestHeaders, e, extractStatusCode(e), extractResponseBody(e));
            throw mapAndThrowStructuredException(properties.getApi().getForgetEndpoint() + "/" + datasetId, "DELETE", e);
        }
    }

    public GraphQueryResult fetchGraph(String datasetName) {
        if (!StringUtils.hasText(datasetName)) {
            throw new DatasetNotFoundException("Dataset name cannot be blank");
        }

        String datasetId = resolveDatasetId(datasetName);
        if (!StringUtils.hasText(datasetId)) {
            log.warn("Failed to resolve dataset ID: Dataset not found in Cognee Cloud for '{}'", datasetName);
            throw new DatasetNotFoundException("Dataset not found in Cognee Cloud");
        }

        long startTime = System.currentTimeMillis();
        HttpHeaders requestHeaders = buildHeaders(MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);
        String relativeUri = properties.getApi().getGraphEndpoint() + "/" + datasetId + "/graph";
        String requestUrl = buildFullUrl(relativeUri);
        try {
            WebClient.RequestHeadersSpec<?> requestSpec = webClient.get()
                    .uri(relativeUri)
                    .headers(headers -> headers.addAll(requestHeaders));

            GraphQueryResult graphResponse = requestSpec
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, errorResponse -> errorResponse.createException().flatMap(Mono::error))
                    .bodyToMono(GraphQueryResult.class)
                    .retryWhen(createRetrySpec())
                    .block();

            availabilityService.recordSuccess();
            long latency = System.currentTimeMillis() - startTime;
            logApiInteraction(relativeUri, "GET", "datasetName=" + datasetName, graphResponse, latency, 200);
            return graphResponse;
        } catch (Exception e) {
            long latency = System.currentTimeMillis() - startTime;
            logApiInteraction(relativeUri, "GET", "datasetName=" + datasetName, e.getMessage(), latency, 500);
            logCogneeFailure(requestUrl, "GET", requestHeaders, e, extractStatusCode(e), extractResponseBody(e));
            throw mapAndThrowStructuredException(relativeUri, "GET", e);
        }
    }

    private String resolveDatasetId(String datasetName) {
        HttpHeaders requestHeaders = buildHeaders(MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);
        String requestUrl = buildFullUrl(properties.getApi().getForgetEndpoint() + "/");
        try {
            WebClient.RequestHeadersSpec<?> requestSpec = webClient.get()
                    .uri(properties.getApi().getForgetEndpoint() + "/")
                    .headers(headers -> headers.addAll(requestHeaders));

            String responseString = requestSpec.retrieve()
                    .onStatus(HttpStatusCode::isError, response -> response.createException().flatMap(Mono::error))
                    .bodyToMono(String.class)
                    .block();
            JsonNode datasetsResponse = StringUtils.hasText(responseString) ? mapper.readTree(responseString) : null;

            if (datasetsResponse != null && datasetsResponse.isArray()) {
                log.debug("Full raw datasetsResponse: {}", datasetsResponse);
                for (JsonNode node : datasetsResponse) {
                    String name = node.has("dataset_name") ? node.path("dataset_name").asText() : node.path("name").asText();
                    if (datasetName.equalsIgnoreCase(name)) {
                        return node.has("dataset_id") ? node.path("dataset_id").asText() : node.path("id").asText();
                    }
                }
            }
        } catch (Exception e) {
            logCogneeFailure(requestUrl, "GET", requestHeaders, e, extractStatusCode(e), extractResponseBody(e));
            log.warn("Failed to resolve Cognee Cloud dataset id for '{}': {}", datasetName, e.getMessage());
        }
        return null;
    }

    private void pollDatasetStatus(KnowledgeGraphResponse response, String datasetId) {
        if (!StringUtils.hasText(datasetId)) {
            log.warn("No dataset id available to poll status for dataset '{}'.", response.getDatasetName());
            return;
        }

        String requestUrl = buildFullUrl(DATASET_STATUS_ENDPOINT) + "?dataset=" + datasetId + "&pipeline=" + COGNIFY_PIPELINE;
        int attempts = 0;
        try {
            while (attempts < STATUS_POLL_MAX_ATTEMPTS) {
                attempts++;
                String statusResponseString = webClient.get()
                        .uri(DATASET_STATUS_ENDPOINT + "?dataset=" + datasetId + "&pipeline=" + COGNIFY_PIPELINE)
                        .retrieve()
                        .onStatus(HttpStatusCode::isError, r -> r.createException().flatMap(Mono::error))
                        .bodyToMono(String.class)
                        .block();
                JsonNode statusResponse = StringUtils.hasText(statusResponseString) ? mapper.readTree(statusResponseString) : null;

                if (statusResponse != null) {
                    String status = null;
                    if (statusResponse.has(datasetId)) {
                        status = statusResponse.get(datasetId).asText();
                    } else if (statusResponse.isObject()) {
                        // pick the first entry if shape differs
                        var it = statusResponse.fieldNames();
                        if (it.hasNext()) {
                            String key = it.next();
                            status = statusResponse.path(key).asText();
                        }
                    }

                    if (status != null) {
                        log.info("Polled dataset status for {} -> {}", datasetId, status);
                        if ("completed".equalsIgnoreCase(status)) {
                            response.setSuccess(true);
                            return;
                        } else if ("failed".equalsIgnoreCase(status)) {
                            response.setSuccess(false);
                            response.setErrors(List.of("Cognee processing failed for dataset id: " + datasetId));
                            return;
                        }
                    }
                }

                try {
                    Thread.sleep(STATUS_POLL_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            log.warn("Dataset status polling timed out after {} attempts for dataset id {}", attempts, datasetId);
            response.setSuccess(false);
            response.setErrors(List.of("Timed out waiting for Cognee dataset processing"));
        } catch (Exception e) {
            logCogneeFailure(requestUrl, "GET", buildHeaders(MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON), e, extractStatusCode(e), extractResponseBody(e));
        }
    }

    private String getDatasetStatusById(String datasetId) {
        if (!StringUtils.hasText(datasetId)) return null;
        try {
            String statusResponseString = webClient.get()
                    .uri(DATASET_STATUS_ENDPOINT + "?dataset=" + datasetId + "&pipeline=" + COGNIFY_PIPELINE)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, r -> r.createException().flatMap(Mono::error))
                    .bodyToMono(String.class)
                    .block();
            JsonNode statusResponse = StringUtils.hasText(statusResponseString) ? mapper.readTree(statusResponseString) : null;

            if (statusResponse != null) {
                if (statusResponse.has(datasetId)) {
                    return statusResponse.get(datasetId).asText();
                } else if (statusResponse.isObject()) {
                    var it = statusResponse.fieldNames();
                    if (it.hasNext()) {
                        String key = it.next();
                        return statusResponse.path(key).asText();
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Failed to get dataset status for {}: {}", datasetId, e.getMessage());
        }
        return null;
    }

    private Retry createRetrySpec() {
        return Retry.backoff(3, Duration.ofSeconds(2))
                .filter(this::shouldRetry);
    }

    private boolean shouldRetry(Throwable throwable) {
        if (throwable instanceof WebClientResponseException webClientResponseException) {
            int status = webClientResponseException.getStatusCode().value();
            return status == 429 || status == 500 || status == 502 || status == 503 || status == 504;
        }
        String message = throwable.getMessage() == null ? "" : throwable.getMessage().toLowerCase();
        return throwable instanceof ConnectException
                || throwable instanceof UnknownHostException
                || throwable instanceof SocketTimeoutException
                || throwable instanceof TimeoutException
                || message.contains("connection reset")
                || message.contains("timed out")
                || message.contains("timeout");
    }

    private RuntimeException mapAndThrowStructuredException(String url, String method, Exception e) {
        Throwable current = e;
        while (current != null) {
            String message = current.getMessage() == null ? "" : current.getMessage().toLowerCase();
            if (current instanceof UnknownHostException || message.contains("unknownhostexception")) {
                throw new CogneeUnavailableException("Remote service host name is unreachable: " + current.getMessage(), e);
            }
            if (current instanceof ConnectException || message.contains("connection refused") || message.contains("connectexception")) {
                throw new CogneeUnavailableException("Connection refused by remote service host: " + current.getMessage(), e);
            }
            if (message.contains("handshake") || message.contains("ssl") || message.contains("tls") || message.contains("fatal alert")) {
                throw new TLSHandshakeException("Secure SSL/TLS handshake failed for endpoint " + url + ": " + current.getMessage(), e);
            }
            if (current instanceof SocketTimeoutException || current instanceof TimeoutException || message.contains("timeout")) {
                throw new CogneeUnavailableException("Request timed out for endpoint " + url, e);
            }
            current = current.getCause();
        }

        if (e instanceof WebClientResponseException webClientResponseException) {
            int status = webClientResponseException.getStatusCode().value();
            if (status == 400 || status == 422) {
                throw new InvalidQuestionException("Invalid request sent to Cognee Cloud: " + webClientResponseException.getResponseBodyAsString());
            }
            if (status == 401) {
                throw new CogneeUnavailableException("Unauthorized: invalid Cognee Cloud API key", e);
            }
            if (status == 403) {
                throw new CogneeUnavailableException("Forbidden: insufficient access to Cognee Cloud", e);
            }
            if (status == 404) {
                throw new DatasetNotFoundException("Dataset or endpoint not found in Cognee Cloud");
            }
            if (status == 429) {
                throw new RateLimitExceededException("Cognee Cloud rate limit exceeded");
            }
            if (status >= 500) {
                throw new HttpServerErrorException("Cognee Cloud server error: " + webClientResponseException.getMessage(), e);
            }
        }

        String errorMessage = extractErrorMessage(e);
        throw new CogneeUnavailableException("Cognee API interaction failure: " + errorMessage, e);
    }

    private void logStructuredException(String url, String method, Exception e) {
        Throwable current = e;
        StringBuilder builder = new StringBuilder();
        while (current != null) {
            if (builder.length() > 0) {
                builder.append(" -> ");
            }
            builder.append(current.getClass().getSimpleName()).append(": ").append(current.getMessage());
            current = current.getCause();
        }
        log.warn("Cognee REST failure. url={}, method={}, causeChain={}", url, method, sanitizeForLog(builder.toString()));
        if (log.isDebugEnabled()) {
            log.debug("Cognee REST failure details", e);
        }
    }

    private boolean hasApiKey() {
        return properties.getApi() != null
                && StringUtils.hasText(properties.getApi().getKey())
                && properties.getApi().getBaseUrl() != null
                && properties.getApi().getBaseUrl().contains("cognee.ai");
    }

    public boolean isMock() {
        return properties.isMock() || (properties.getApi() != null && properties.getApi().isMock());
    }
}
