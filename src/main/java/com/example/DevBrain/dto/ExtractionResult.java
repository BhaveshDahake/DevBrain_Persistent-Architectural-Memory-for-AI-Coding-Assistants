package com.example.DevBrain.dto;

public class ExtractionResult {
    private String datasetName;
    private long totalFiles;
    private long keptFiles;
    private long skippedFiles;
    private String cleanPath;
    private long processingTimeMs;

    private java.util.List<String> addedFiles = new java.util.ArrayList<>();
    private java.util.List<String> modifiedFiles = new java.util.ArrayList<>();
    private java.util.List<String> removedFiles = new java.util.ArrayList<>();

    public ExtractionResult() {}

    public String getDatasetName() { return datasetName; }
    public void setDatasetName(String datasetName) { this.datasetName = datasetName; }

    public long getTotalFiles() { return totalFiles; }
    public void setTotalFiles(long totalFiles) { this.totalFiles = totalFiles; }

    public long getKeptFiles() { return keptFiles; }
    public void setKeptFiles(long keptFiles) { this.keptFiles = keptFiles; }

    public long getSkippedFiles() { return skippedFiles; }
    public void setSkippedFiles(long skippedFiles) { this.skippedFiles = skippedFiles; }

    public String getCleanPath() { return cleanPath; }
    public void setCleanPath(String cleanPath) { this.cleanPath = cleanPath; }

    public long getProcessingTimeMs() { return processingTimeMs; }
    public void setProcessingTimeMs(long processingTimeMs) { this.processingTimeMs = processingTimeMs; }

    public java.util.List<String> getAddedFiles() { return addedFiles; }
    public void setAddedFiles(java.util.List<String> addedFiles) { this.addedFiles = addedFiles; }

    public java.util.List<String> getModifiedFiles() { return modifiedFiles; }
    public void setModifiedFiles(java.util.List<String> modifiedFiles) { this.modifiedFiles = modifiedFiles; }

    public java.util.List<String> getRemovedFiles() { return removedFiles; }
    public void setRemovedFiles(java.util.List<String> removedFiles) { this.removedFiles = removedFiles; }
}
