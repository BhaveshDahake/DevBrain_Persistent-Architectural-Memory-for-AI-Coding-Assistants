package com.example.DevBrain.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {
    @Builder.Default
    private boolean success = false;
    private String errorCode;
    private String error;
    private String message;
    private Integer status;
    private Instant timestamp;
    private String path;
    private String requestId;
    private Banner banner;

    @Data
    @Builder
    public static class Banner {
        private String type;
        private String title;
        private String message;
    }
}
