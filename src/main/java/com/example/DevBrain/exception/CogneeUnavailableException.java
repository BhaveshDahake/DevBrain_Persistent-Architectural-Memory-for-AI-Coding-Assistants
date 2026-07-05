package com.example.DevBrain.exception;

public class CogneeUnavailableException extends RuntimeException {
    public CogneeUnavailableException(String message) {
        super(message);
    }
    public CogneeUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
