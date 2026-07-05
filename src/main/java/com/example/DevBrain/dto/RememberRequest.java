package com.example.DevBrain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class RememberRequest {
    
    @JsonProperty("dataset_name")
    private String datasetName;
    
    private List<RememberDocument> documents;

    public RememberRequest() {}

    public String getDatasetName() { return datasetName; }
    public void setDatasetName(String datasetName) { this.datasetName = datasetName; }

    public List<RememberDocument> getDocuments() { return documents; }
    public void setDocuments(List<RememberDocument> documents) { this.documents = documents; }
}
