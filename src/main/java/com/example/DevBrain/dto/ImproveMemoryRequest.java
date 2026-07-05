package com.example.DevBrain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ImproveMemoryRequest {
    @NotBlank(message = "messageId cannot be blank")
    @Size(max = 255)
    private String messageId;

    @NotBlank(message = "datasetName cannot be blank")
    @Size(max = 255)
    private String datasetName;

    @NotNull(message = "positive feedback indicator must be provided")
    private Boolean positive;
}
