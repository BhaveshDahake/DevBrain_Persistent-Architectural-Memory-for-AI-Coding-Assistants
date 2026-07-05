package com.example.DevBrain.service;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatasetServiceTest {

    private final DatasetService service = new DatasetService();

    @Test
    void uploadDatasetRejectsTraversalInOriginalFilename() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "../../evil.zip",
                "application/zip",
                "content".getBytes(StandardCharsets.UTF_8)
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.uploadDataset(file, "demo")
        );

        assertTrue(exception.getMessage().contains("Only .zip files are allowed"));
    }
}
