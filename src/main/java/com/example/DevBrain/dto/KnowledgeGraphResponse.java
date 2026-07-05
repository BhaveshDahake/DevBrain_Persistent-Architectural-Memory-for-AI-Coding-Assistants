package com.example.DevBrain.dto;

import java.util.List;

public class KnowledgeGraphResponse {
    
    private boolean success;
    private String datasetName;
    private long filesProcessed;
    private int batchesSent;
    private String knowledgeGraphId;
    private String status;
    private String source;
    private List<String> errors;

    public KnowledgeGraphResponse() {}

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getDatasetName() { return datasetName; }
    public void setDatasetName(String datasetName) { this.datasetName = datasetName; }

    public long getFilesProcessed() { return filesProcessed; }
    public void setFilesProcessed(long filesProcessed) { this.filesProcessed = filesProcessed; }

    public int getBatchesSent() { return batchesSent; }
    public void setBatchesSent(int batchesSent) { this.batchesSent = batchesSent; }

    public String getKnowledgeGraphId() { return knowledgeGraphId; }
    public void setKnowledgeGraphId(String knowledgeGraphId) { this.knowledgeGraphId = knowledgeGraphId; }

    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> errors) { this.errors = errors; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
