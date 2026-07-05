package com.example.DevBrain.service;

import com.example.DevBrain.dto.UploadResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Service
public class DatasetService {

    private static final Logger log = LoggerFactory.getLogger(DatasetService.class);
    private static final String UPLOAD_DIR = "uploads";

    public DatasetService() {
    }

    public UploadResponse uploadDataset(MultipartFile file, String datasetName) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("ZIP file is required and cannot be empty.");
        }
        if (!StringUtils.hasText(datasetName)) {
            throw new IllegalArgumentException("Dataset name is required.");
        }

        String originalFilename = file.getOriginalFilename();
        validateOriginalFilename(originalFilename, file.getContentType());

        String safeDatasetName = sanitizeDatasetName(datasetName);
        Path baseDir = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();
        Path uploadPath = baseDir.resolve(safeDatasetName).normalize();
        if (!uploadPath.startsWith(baseDir)) {
            throw new IllegalArgumentException("Invalid dataset path.");
        }

        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Created upload directory for dataset {}", safeDatasetName);
            }

            String safeOriginalName = StringUtils.cleanPath(originalFilename);
            if (!StringUtils.hasText(safeOriginalName) || safeOriginalName.contains("..") || safeOriginalName.contains("/") || safeOriginalName.contains("\\")) {
                throw new IllegalArgumentException("Only .zip files are allowed.");
            }

            String uniqueFilename = UUID.randomUUID() + "_" + safeOriginalName;
            Path filePath = uploadPath.resolve(uniqueFilename).toAbsolutePath().normalize();
            if (!filePath.startsWith(baseDir)) {
                throw new IllegalArgumentException("Invalid dataset path.");
            }

            file.transferTo(filePath.toFile());
            log.info("Saved dataset {} to {}", safeDatasetName, filePath.getFileName());

            UploadResponse response = new UploadResponse();
            response.setSuccess(true);
            response.setMessage("Dataset uploaded successfully");
            response.setDatasetName(datasetName);
            response.setFileName(uniqueFilename);
            response.setSavedLocation(filePath.toString());
            response.setUploadedAt(LocalDateTime.now());
            response.setSize(file.getSize());

            return response;

        } catch (IOException e) {
            log.error("Failed to store file for dataset {}", safeDatasetName, e);
            throw new RuntimeException("Failed to store dataset: " + e.getMessage(), e);
        }
    }

    private void validateOriginalFilename(String originalFilename, String contentType) {
        if (!StringUtils.hasText(originalFilename)) {
            throw new IllegalArgumentException("Only .zip files are allowed.");
        }

        String normalizedName = StringUtils.cleanPath(originalFilename);
        if (!StringUtils.hasText(normalizedName)
                || normalizedName.contains("..")
                || normalizedName.contains("/")
                || normalizedName.contains("\\")) {
            throw new IllegalArgumentException("Only .zip files are allowed.");
        }

        String lowerName = normalizedName.toLowerCase(Locale.ROOT);
        boolean isZipName = lowerName.endsWith(".zip");
        boolean isZipContentType = StringUtils.hasText(contentType)
                && (contentType.equalsIgnoreCase("application/zip")
                || contentType.equalsIgnoreCase("application/x-zip-compressed")
                || contentType.equalsIgnoreCase("application/octet-stream"));

        if (!isZipName && !isZipContentType) {
            throw new IllegalArgumentException("Only .zip files are allowed.");
        }
    }

    private String sanitizeDatasetName(String datasetName) {
        String trimmed = StringUtils.trimWhitespace(datasetName);
        if (!StringUtils.hasText(trimmed)) {
            throw new IllegalArgumentException("Dataset name is required.");
        }

        String sanitized = trimmed.replaceAll("[^a-zA-Z0-9_-]", "");
        if (!StringUtils.hasText(sanitized)) {
            throw new IllegalArgumentException("Dataset name is required.");
        }
        return sanitized;
    }
}
