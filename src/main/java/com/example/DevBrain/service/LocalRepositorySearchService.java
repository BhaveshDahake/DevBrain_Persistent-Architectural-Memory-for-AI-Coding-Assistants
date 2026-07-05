package com.example.DevBrain.service;

import com.example.DevBrain.config.DatasetProperties;
import com.example.DevBrain.dto.RepositoryContext;
import com.example.DevBrain.dto.SearchResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LocalRepositorySearchService {

    private static final Logger log = LoggerFactory.getLogger(LocalRepositorySearchService.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private final DatasetProperties datasetProperties;

    public LocalRepositorySearchService(DatasetProperties datasetProperties) {
        this.datasetProperties = datasetProperties;
    }

    public void persistMetadata(String datasetName, String cleanFolderPath) {
        if (!StringUtils.hasText(datasetName) || !StringUtils.hasText(cleanFolderPath)) {
            return;
        }

        Path workspacePath = Paths.get(datasetProperties.getWorkspace(), datasetName).toAbsolutePath().normalize();
        Path metadataPath = workspacePath.resolve("local-metadata.json");
        try {
            Files.createDirectories(workspacePath);
            List<Map<String, String>> entries = buildEntries(Path.of(cleanFolderPath));
            mapper.writeValue(metadataPath.toFile(), entries);
        } catch (IOException e) {
            log.debug("Unable to persist local repository metadata for {}", datasetName, e);
        }
    }

    public SearchResult search(String datasetName, String question) {
        if (!StringUtils.hasText(question)) {
            return SearchResult.builder().answer("").contexts(Collections.emptyList()).source("LOCAL").status("LOCAL_ONLY").build();
        }

        Path workspacePath = Paths.get(datasetProperties.getWorkspace(), datasetName).toAbsolutePath().normalize();
        Path metadataPath = workspacePath.resolve("local-metadata.json");
        List<Map<String, String>> entries = readEntries(metadataPath);
        if (entries.isEmpty()) {
            return SearchResult.builder().answer("").contexts(Collections.emptyList()).source("LOCAL").status("LOCAL_ONLY").build();
        }

        List<String> tokens = tokenize(question);
        List<RepositoryContext> contexts = entries.stream()
                .filter(entry -> matches(entry, tokens))
                .map(entry -> RepositoryContext.builder()
                        .file(entry.get("path"))
                        .summary(entry.get("preview"))
                        .score(1.0)
                        .build())
                .collect(Collectors.toCollection(ArrayList::new));

        String answer = contexts.isEmpty() ? "I do not know based on the provided repository context." : "Local repository context matched the request.";
        return SearchResult.builder()
                .answer(answer)
                .contexts(contexts)
                .processing(false)
                .status("LOCAL_ONLY")
                .source("LOCAL")
                .build();
    }

    private List<Map<String, String>> readEntries(Path metadataPath) {
        if (!Files.exists(metadataPath)) {
            return Collections.emptyList();
        }
        try {
            return mapper.readValue(metadataPath.toFile(), new TypeReference<>() {});
        } catch (IOException e) {
            log.debug("Unable to read local repository metadata from {}", metadataPath, e);
            return Collections.emptyList();
        }
    }

    private List<Map<String, String>> buildEntries(Path cleanFolder) {
        if (!Files.exists(cleanFolder) || !Files.isDirectory(cleanFolder)) {
            return Collections.emptyList();
        }
        try (java.util.stream.Stream<Path> paths = Files.walk(cleanFolder)) {
            return paths.filter(Files::isRegularFile)
                    .map(path -> {
                        try {
                            String content = Files.readString(path);
                            String preview = content.length() > 400 ? content.substring(0, 400) : content;
                            return Map.of(
                                    "path", cleanFolder.relativize(path).toString().replace('\\', '/'),
                                    "preview", preview
                            );
                        } catch (IOException e) {
                            return Map.of("path", cleanFolder.relativize(path).toString().replace('\\', '/'), "preview", "");
                        }
                    })
                    .collect(Collectors.toCollection(ArrayList::new));
        } catch (IOException e) {
            log.debug("Unable to scan local repository metadata from {}", cleanFolder, e);
            return Collections.emptyList();
        }
    }

    private List<String> tokenize(String value) {
        return java.util.Arrays.stream(value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", " ").trim().split("\\s+"))
                .filter(token -> token.length() > 1)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private boolean matches(Map<String, String> entry, List<String> tokens) {
        if (tokens.isEmpty()) {
            return true;
        }
        String haystack = (entry.get("path") + " " + entry.get("preview")).toLowerCase(Locale.ROOT);
        return tokens.stream().anyMatch(haystack::contains);
    }
}
