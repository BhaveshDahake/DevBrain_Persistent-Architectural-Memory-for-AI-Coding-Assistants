package com.example.DevBrain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GraphResponse {
    private Boolean success;
    private String message;
    private List<GraphNode> nodes;
    private List<GraphLink> links;
}
