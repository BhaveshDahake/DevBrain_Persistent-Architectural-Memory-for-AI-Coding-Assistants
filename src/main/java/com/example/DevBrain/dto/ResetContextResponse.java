package com.example.DevBrain.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResetContextResponse {
    private boolean success;
    private String datasetName;
    private String message;
}
