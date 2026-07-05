package com.example.DevBrain.controller;

import com.example.DevBrain.dto.UploadResponse;
import com.example.DevBrain.dto.ExtractionResult;
import com.example.DevBrain.dto.KnowledgeGraphResponse;
import com.example.DevBrain.service.DatasetService;
import com.example.DevBrain.service.DatasetExtractionService;
import com.example.DevBrain.service.CogneeClientService;
import com.example.DevBrain.exception.CogneeUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/datasets")
@CrossOrigin(origins = "*") // Allows the React frontend to communicate with this API
public class DatasetController {
    
    private static final Logger log = LoggerFactory.getLogger(DatasetController.class);
    
    private final DatasetService datasetService;
    private final DatasetExtractionService datasetExtractionService;
    private final CogneeClientService cogneeClientService;

    @Autowired
    public DatasetController(DatasetService datasetService, 
                             DatasetExtractionService datasetExtractionService,
                             CogneeClientService cogneeClientService) {
        this.datasetService = datasetService;
        this.datasetExtractionService = datasetExtractionService;
        this.cogneeClientService = cogneeClientService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> uploadDataset(
            @RequestParam("file") MultipartFile file,
            @RequestParam("datasetName") String datasetName,
            jakarta.servlet.http.HttpServletRequest request) {
            
        String userId = (String) request.getAttribute("userId");
        String scopedDatasetName = (userId != null) ? userId + "_" + datasetName : datasetName;
        log.info("Received upload request for dataset: {} (scoped: {})", datasetName, scopedDatasetName);
        
        // 1. Validate and save the raw uploaded file
        UploadResponse response = datasetService.uploadDataset(file, scopedDatasetName);
        
        // 2. Extract and filter contents from the saved absolute zip path
        Path savedZipPath = Paths.get(response.getSavedLocation()).toAbsolutePath().normalize();
        ExtractionResult extractionResult = datasetExtractionService.processDataset(savedZipPath, scopedDatasetName);
        response.setExtractionResult(extractionResult);

        // 3. Send cleaned source files to Cognee API for Knowledge Graph generation
        if (extractionResult.getKeptFiles() > 0) {
            KnowledgeGraphResponse kgResponse = cogneeClientService.rememberRepository(scopedDatasetName, extractionResult.getCleanPath());
            response.setKnowledgeGraphResult(kgResponse);
        }
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
