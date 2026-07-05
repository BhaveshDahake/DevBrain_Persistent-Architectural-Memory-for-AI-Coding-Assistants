package com.example.DevBrain.service;

import com.example.DevBrain.config.DatasetProperties;
import com.example.DevBrain.dto.ForgetResult;
import com.example.DevBrain.dto.ResetContextResponse;
import com.example.DevBrain.exception.DatasetNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContextResetServiceImpl implements ContextResetService {

    private final CogneeClientService cogneeClientService;
    private final DatasetProperties datasetProperties;

    @Override
    public ResetContextResponse reset(String datasetName) {
        log.info("Resetting repository context requested for dataset: {}", datasetName);
        long startTime = System.currentTimeMillis();

        if (!StringUtils.hasText(datasetName)) {
            throw new IllegalArgumentException("Dataset name cannot be blank");
        }

        // 1. Request Memory Removal from Cognee
        ForgetResult forgetResult = cogneeClientService.forgetDataset(datasetName);
        boolean remoteSuccess = forgetResult != null && forgetResult.isSuccess();

        // 2. Clear Local Metadata & Files
        boolean localSuccess = true;
        try {
            // Remove from Workspace
            Path workspacePath = Paths.get(datasetProperties.getWorkspace(), datasetName).toAbsolutePath().normalize();
            if (Files.exists(workspacePath)) {
                FileSystemUtils.deleteRecursively(workspacePath);
                log.info("Deleted local workspace path: {}", workspacePath);
            }

            // Remove from Uploads (assuming "uploads" is at root like in DatasetService)
            Path uploadPath = Paths.get("uploads", datasetName).toAbsolutePath().normalize();
            if (Files.exists(uploadPath)) {
                FileSystemUtils.deleteRecursively(uploadPath);
                log.info("Deleted local uploads path: {}", uploadPath);
            }
            
            // Remove from Cache if present
            Path cachePath = Paths.get("cache", datasetName).toAbsolutePath().normalize();
            if (Files.exists(cachePath)) {
                FileSystemUtils.deleteRecursively(cachePath);
                log.info("Deleted local cache path: {}", cachePath);
            }

        } catch (IOException e) {
            log.error("Failed to delete local workspace files for dataset {}", datasetName, e);
            localSuccess = false;
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("Context reset completed in {}ms. Remote Success: {}, Local Success: {}", duration, remoteSuccess, localSuccess);

        if (localSuccess) {
            String msg = remoteSuccess 
                ? "Repository context removed completely." 
                : "Repository context removed locally, but failed to remove from Cognee Cloud (local fallback graph will be cleared).";
            return ResetContextResponse.builder()
                    .success(true)
                    .datasetName(datasetName)
                    .message(msg)
                    .build();
        } else {
            throw new RuntimeException("Failed to delete local repository files during context reset.");
        }
    }
}
