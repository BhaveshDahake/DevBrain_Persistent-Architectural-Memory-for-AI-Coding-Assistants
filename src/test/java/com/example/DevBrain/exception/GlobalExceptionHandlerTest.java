package com.example.DevBrain.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    @Test
    void invalidRequestReturnsStructuredErrorPayload() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/chat/ask");
        request.setRequestURI("/api/chat/ask");

        ResponseEntity<ApiErrorResponse> response = handler.handleValidationExceptions(new IllegalArgumentException("bad input"), request);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("BAD_REQUEST");
        assertThat(response.getBody().getPath()).isEqualTo("/api/chat/ask");
        assertThat(response.getBody().getRequestId()).isNotBlank();
    }
}
