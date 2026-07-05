package com.example.DevBrain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CogneeDatasetResponse {
    private String id;
    private String name;
}
