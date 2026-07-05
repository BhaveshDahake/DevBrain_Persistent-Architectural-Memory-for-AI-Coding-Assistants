package com.example.DevBrain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GraphNode {
    private String id;
    private String label;
    private String type;
    private String path;
    private Map<String, Object> metadata;
}
