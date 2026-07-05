package com.example.DevBrain.service;

import com.example.DevBrain.dto.ExtractionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.nio.file.Path;

@Service
public class DatasetExtractionService {
    
    private static final Logger log = LoggerFactory.getLogger(DatasetExtractionService.class);
    
    private final ZipExtractor zipExtractor;

    public DatasetExtractionService(ZipExtractor zipExtractor) {
        this.zipExtractor = zipExtractor;
    }

    /**
     * Processes an uploaded zip repository by applying directory and file type cleaning,
     * maintaining folder structure, and returning operation metrics.
     */
    public ExtractionResult processDataset(Path zipFilePath, String datasetName) {
        log.info("Starting processing pipeline for dataset: {}", datasetName);
        
        // Extractor natively sanitizes entries, applies filters, and streams directly to disk.
        ExtractionResult result = zipExtractor.extract(zipFilePath, datasetName);
        
        log.info("Finished processing pipeline for dataset: {}", datasetName);
        return result;
    }
}
