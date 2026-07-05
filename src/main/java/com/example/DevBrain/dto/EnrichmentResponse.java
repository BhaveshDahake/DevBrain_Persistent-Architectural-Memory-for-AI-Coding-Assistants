package com.example.DevBrain.dto;

import lombok.Data;

@Data
public class EnrichmentResponse {
    private Boolean success;
    private String jobId;
    private String status;
}
