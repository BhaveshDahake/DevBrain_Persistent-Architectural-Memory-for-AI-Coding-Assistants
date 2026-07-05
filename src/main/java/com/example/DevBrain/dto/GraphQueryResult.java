package com.example.DevBrain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GraphQueryResult {
    // Pattern A
    private List<Map<String, Object>> nodes;
    private List<Map<String, Object>> edges;
    
    // Pattern B
    private List<Map<String, Object>> entities;
    private List<Map<String, Object>> relationships;
}
