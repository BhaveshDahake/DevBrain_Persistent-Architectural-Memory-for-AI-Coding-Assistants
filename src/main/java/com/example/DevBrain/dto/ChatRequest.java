package com.example.DevBrain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChatRequest {

    @NotBlank(message = "Question cannot be empty")
    @Size(max = 3000, message = "Question exceeds the maximum length of 3000 characters")
    private String question;

    @NotBlank(message = "Dataset name cannot be empty")
    @Size(max = 255, message = "Dataset name exceeds the maximum length of 255 characters")
    private String datasetName;
}
