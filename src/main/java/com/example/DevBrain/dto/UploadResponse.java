package com.example.DevBrain.dto;

import java.time.LocalDateTime;

public class UploadResponse {
    private boolean success;
    private String message;
    private String datasetName;
    private String fileName;
    private String savedLocation;
    private LocalDateTime uploadedAt;
    private long size;
    private ExtractionResult extractionResult;
    private KnowledgeGraphResponse knowledgeGraphResult;

    public UploadResponse() {}

    public KnowledgeGraphResponse getKnowledgeGraphResult() { return knowledgeGraphResult; }
    public void setKnowledgeGraphResult(KnowledgeGraphResponse knowledgeGraphResult) { this.knowledgeGraphResult = knowledgeGraphResult; }

    public ExtractionResult getExtractionResult() { return extractionResult; }
    public void setExtractionResult(ExtractionResult extractionResult) { this.extractionResult = extractionResult; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getDatasetName() { return datasetName; }
    public void setDatasetName(String datasetName) { this.datasetName = datasetName; }
    
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    
    public String getSavedLocation() { return savedLocation; }
    public void setSavedLocation(String savedLocation) { this.savedLocation = savedLocation; }
    
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
    
    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }
}
