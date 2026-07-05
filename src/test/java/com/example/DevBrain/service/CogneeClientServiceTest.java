package com.example.DevBrain.service;

import com.example.DevBrain.config.CogneeProperties;
import com.example.DevBrain.dto.KnowledgeGraphResponse;
import com.example.DevBrain.dto.SearchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CogneeClientServiceTest {

    private WebClient webClient;
    private CogneeProperties properties;
    private CogneeAvailabilityService availabilityService;
    private LocalRepositorySearchService localRepositorySearchService;
    private CogneeClientService service;

    @BeforeEach
    public void setUp() {
        webClient = Mockito.mock(WebClient.class);
        properties = Mockito.mock(CogneeProperties.class);
        availabilityService = Mockito.mock(CogneeAvailabilityService.class);
        localRepositorySearchService = Mockito.mock(LocalRepositorySearchService.class);
        service = new CogneeClientService(webClient, properties, availabilityService, localRepositorySearchService);
    }

    @Test
    public void testRememberRepositoryThrowsWhenFolderMissing() {
        when(availabilityService.canAttempt()).thenReturn(true);
        assertThrows(RuntimeException.class, () -> service.rememberRepository("test-repo", "c:/nonexistent/path/for/test"));
    }

    @Test
    public void testSearchRepositoryRejectsEmptyQuestion() {
        assertThrows(RuntimeException.class, () -> service.searchRepository("", "test-repo"));
    }

    @Test
    public void testExtractErrorMessagePrefersResponseBodyWhenPresent() {
        WebClientResponseException exception = new WebClientResponseException(
                "upstream message",
                502,
                "Bad Gateway",
                HttpHeaders.EMPTY,
                "{\"error\":\"bad gateway\"}".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
        );

        assertEquals("{\"error\":\"bad gateway\"}", CogneeClientService.extractErrorMessage(exception));
    }

    @Test
    public void testRememberRepositorySucceedsWhenCloudIsHealthy() throws Exception {
        Path tempDir = Files.createTempDirectory("cognee-test");
        Path repoFile = tempDir.resolve("README.md");
        Files.writeString(repoFile, "hello world");

        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(String.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(BodyInserter.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.exchangeToMono(any(Function.class))).thenReturn(Mono.just("{\"status\":\"completed\"}"));
        when(properties.getMaxFileSize()).thenReturn(org.springframework.util.unit.DataSize.ofKilobytes(1));
        when(properties.getApi()).thenReturn(new CogneeProperties.Api());
        when(availabilityService.canAttempt()).thenReturn(true);

        KnowledgeGraphResponse response = service.rememberRepository("demo", tempDir.toString());

        assertTrue(response.isSuccess());
        assertEquals("COGNEE", response.getSource());
        assertEquals("COGNEE_READY", response.getStatus());
    }

    @Test
    public void testRememberRepositoryFallsBackOnTimeout() throws Exception {
        Path tempDir = Files.createTempDirectory("cognee-timeout");
        Files.writeString(tempDir.resolve("README.md"), "hello");

        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(String.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(BodyInserter.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.exchangeToMono(any(Function.class))).thenReturn(Mono.error(new TimeoutException("timed out")));
        when(properties.getMaxFileSize()).thenReturn(org.springframework.util.unit.DataSize.ofKilobytes(1));
        when(properties.getApi()).thenReturn(new CogneeProperties.Api());
        when(availabilityService.canAttempt()).thenReturn(true);
        when(availabilityService.shouldUseFallback(any(Throwable.class))).thenReturn(true);

        KnowledgeGraphResponse response = service.rememberRepository("demo", tempDir.toString());

        assertFalse(response.isSuccess());
        assertEquals("LOCAL_ONLY", response.getStatus());
        assertEquals("LOCAL", response.getSource());
    }

    @Test
    public void testRememberRepositoryFallsBackOnConnectionReset() throws Exception {
        Path tempDir = Files.createTempDirectory("cognee-reset");
        Files.writeString(tempDir.resolve("README.md"), "hello");

        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(String.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(BodyInserter.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.exchangeToMono(any(Function.class))).thenReturn(Mono.error(new ConnectException("connection reset")));
        when(properties.getMaxFileSize()).thenReturn(org.springframework.util.unit.DataSize.ofKilobytes(1));
        when(properties.getApi()).thenReturn(new CogneeProperties.Api());
        when(availabilityService.canAttempt()).thenReturn(true);
        when(availabilityService.shouldUseFallback(any(Throwable.class))).thenReturn(true);

        KnowledgeGraphResponse response = service.rememberRepository("demo", tempDir.toString());

        assertFalse(response.isSuccess());
        assertEquals("LOCAL_ONLY", response.getStatus());
        assertEquals("LOCAL", response.getSource());
    }

    @Test
    public void testSearchRepositoryFallsBackToLocalSearch() {
        when(availabilityService.isFallbackActive()).thenReturn(true);
        SearchResult localResult = SearchResult.builder().answer("local").contexts(java.util.List.of()).source("LOCAL").status("LOCAL_ONLY").build();
        when(localRepositorySearchService.search("demo", "question")).thenReturn(localResult);

        SearchResult result = service.searchRepository("question", "demo");

        assertEquals("LOCAL", result.getSource());
        assertEquals("LOCAL_ONLY", result.getStatus());
    }

    @Test
    public void testAvailabilityServiceRecoversAfterCooldown() {
        CogneeProperties properties = new CogneeProperties();
        properties.getFallback().setFailureThreshold(1);
        properties.getFallback().setCooldown(Duration.ofSeconds(1));
        properties.getRequest().setTimeout(Duration.ofSeconds(1));

        MutableClock clock = new MutableClock(Instant.parse("2026-07-05T00:00:00Z"));
        CogneeAvailabilityService service = new CogneeAvailabilityService(properties, clock);

        service.recordFailure(new TimeoutException("timed out"));
        assertFalse(service.canAttempt());

        clock.advance(Duration.ofSeconds(2));
        assertTrue(service.canAttempt());
    }

    private static class MutableClock extends Clock {
        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }

        private void advance(Duration duration) {
            instant = instant.plus(duration);
        }
    }
}
