package com.example.DevBrain.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ForgetResult {
    private boolean success;
    private String status;
}
