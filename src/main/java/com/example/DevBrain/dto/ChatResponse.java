package com.example.DevBrain.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ChatResponse {
    private boolean success;
    private String answer;
    private String datasetName;
    private Instant timestamp;
}
