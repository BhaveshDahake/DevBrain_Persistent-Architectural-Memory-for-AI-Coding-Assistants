package com.example.DevBrain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GraphLink {
    private String source;
    private String target;
    private String relationship;
    private Double weight;
}
