package com.example.DevBrain.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import jakarta.validation.ConstraintViolationException;
import org.springframework.util.StringUtils;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.nio.file.NoSuchFileException;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @Value("${app.cognee.expose-upstream-errors:${COGNEE_EXPOSE_UPSTREAM_ERRORS:false}}")
    private boolean exposeUpstreamErrors;

    private ApiErrorResponse buildResponse(String errorCode, String message, ApiErrorResponse.Banner banner,
                                           HttpServletRequest request, HttpStatus status) {
        return ApiErrorResponse.builder()
                .success(false)
                .errorCode(errorCode)
                .error(errorCode)
                .message(message)
                .status(status.value())
                .timestamp(Instant.now())
                .path(request != null ? request.getRequestURI() : null)
                .requestId(UUID.randomUUID().toString())
                .banner(banner)
                .build();
    }

    // 400 BAD_REQUEST
    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            ConstraintViolationException.class,
            HttpMessageNotReadableException.class,
            UploadValidationException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ApiErrorResponse> handleValidationExceptions(Exception ex, HttpServletRequest request) {
        log.warn("Validation failure: {}", sanitizeDetail(ex.getMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                buildResponse("BAD_REQUEST", "Invalid request format or missing required fields.", null, request, HttpStatus.BAD_REQUEST)
        );
    }

    // 413 PAYLOAD_TOO_LARGE
    @ExceptionHandler({
            MaxUploadSizeExceededException.class,
            MultipartException.class
    })
    public ResponseEntity<ApiErrorResponse> handleUploadSizeExceptions(Exception ex, HttpServletRequest request) {
        log.warn("Upload size exceeded: {}", sanitizeDetail(ex.getMessage()));
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(
                buildResponse("PAYLOAD_TOO_LARGE", "Uploaded dataset exceeds allowed size.", null, request, HttpStatus.PAYLOAD_TOO_LARGE)
        );
    }

    // 504 GATEWAY_TIMEOUT
    @ExceptionHandler({
            TimeoutException.class,
            WebClientRequestException.class,
            SocketTimeoutException.class
    })
    public ResponseEntity<ApiErrorResponse> handleTimeouts(Exception ex, HttpServletRequest request) {
        log.warn("Gateway timeout: {}", sanitizeDetail(ex.getMessage()));
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(
                buildResponse("GATEWAY_TIMEOUT", "Repository processing timed out.", 
                        ApiErrorResponse.Banner.builder()
                                .type("warning")
                                .title("Temporary Service Delay")
                                .message("Try again in a moment.")
                                .build(), request, HttpStatus.GATEWAY_TIMEOUT)
        );
    }

    // 503 SERVICE_UNAVAILABLE
    @ExceptionHandler({
            WebClientResponseException.class,
            ConnectException.class,
            CogneeUnavailableException.class,
            TLSHandshakeException.class,
            HttpServerErrorException.class
    })
    public ResponseEntity<ApiErrorResponse> handleExternalFailures(Exception ex, HttpServletRequest request) {
        log.warn("External service failure: {}", sanitizeDetail(ex.getMessage()));

        String userMessage = "Repository memory service is temporarily unavailable.";
        String title = "Service Unavailable";
        String bannerMessage = "We are experiencing temporary connectivity issues.";
        String detail = sanitizeDetail(ex.getMessage());

        if (ex instanceof TLSHandshakeException) {
            userMessage = "Secure communication error with Cognee Cloud memory layer.";
            title = "Security Handshake Error";
            bannerMessage = "SSL/TLS handshake failed. Please check network/proxy parameters.";
        } else if (ex instanceof HttpServerErrorException) {
            userMessage = "Cognee Cloud API service returned an unexpected internal error.";
            title = "API Service Error";
            bannerMessage = "Remote server experienced a 5xx error. Try again shortly.";
        } else if (ex instanceof WebClientResponseException webClientResponseException) {
            detail = sanitizeDetail(webClientResponseException.getResponseBodyAsString());
            if (!StringUtils.hasText(detail)) {
                detail = sanitizeDetail(webClientResponseException.getMessage());
            }
        } else if (ex.getCause() instanceof WebClientResponseException webClientResponseException) {
            detail = sanitizeDetail(webClientResponseException.getResponseBodyAsString());
            if (!StringUtils.hasText(detail)) {
                detail = sanitizeDetail(webClientResponseException.getMessage());
            }
        }

        if (exposeUpstreamErrors && StringUtils.hasText(detail)) {
            userMessage = detail;
            bannerMessage = "Cognee Cloud returned an upstream error.";
        }

        ApiErrorResponse response = buildResponse("SERVICE_UNAVAILABLE", userMessage,
                ApiErrorResponse.Banner.builder()
                        .type("warning")
                        .title(title)
                        .message(bannerMessage)
                        .build(), request, HttpStatus.SERVICE_UNAVAILABLE);
        response.setErrorCode(response.getErrorCode());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    // 404 NOT_FOUND
    @ExceptionHandler({
            DatasetNotFoundException.class,
            IllegalStateException.class,
            NoSuchFileException.class
    })
    public ResponseEntity<ApiErrorResponse> handleNotFound(Exception ex, HttpServletRequest request) {
        log.warn("Resource not found: {}", sanitizeDetail(ex.getMessage()));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                buildResponse("NOT_FOUND", "The requested dataset or file was not found.", null, request, HttpStatus.NOT_FOUND)
        );
    }
    
    // 429 TOO_MANY_REQUESTS
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleRateLimit(RateLimitExceededException ex, HttpServletRequest request) {
        log.warn("Rate limit exceeded: {}", sanitizeDetail(ex.getMessage()));
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(
                buildResponse("TOO_MANY_REQUESTS", "Rate limit exceeded.", 
                        ApiErrorResponse.Banner.builder()
                                .type("warning")
                                .title("Slow Down")
                                .message("You are making requests too quickly. Please wait a moment.")
                                .build(), request, HttpStatus.TOO_MANY_REQUESTS)
        );
    }

    private String sanitizeDetail(String detail) {
        if (!StringUtils.hasText(detail)) {
            return "";
        }
        return detail.replaceAll("[\\r\\n]+", " ").replaceAll("\\s{2,}", " ").trim();
    }

    // 500 INTERNAL_SERVER_ERROR (Fallback)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleAllUncaughtException(Exception ex, HttpServletRequest request) {
        log.error("Unexpected server error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                buildResponse("INTERNAL_SERVER_ERROR", "Unexpected server error occurred.", null, request, HttpStatus.INTERNAL_SERVER_ERROR)
        );
    }
}
