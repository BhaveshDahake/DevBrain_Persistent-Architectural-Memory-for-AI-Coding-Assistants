package com.example.DevBrain.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImproveMemoryResponse {
    private boolean success;
    private String message;
    private boolean enrichmentTriggered;
}
