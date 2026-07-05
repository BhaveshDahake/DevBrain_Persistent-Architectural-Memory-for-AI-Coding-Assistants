package com.example.DevBrain.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EnrichmentRequest {
    private String dataset;
    private String messageId;
    @Builder.Default
    private String mode = "ENRICH";
}
