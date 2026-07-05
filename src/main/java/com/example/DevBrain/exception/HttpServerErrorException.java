package com.example.DevBrain.exception;

/**
 * Custom runtime exception representing remote HTTP server error responses (5xx status codes).
 */
public class HttpServerErrorException extends RuntimeException {

    public HttpServerErrorException(String message) {
        super(message);
    }

    public HttpServerErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
