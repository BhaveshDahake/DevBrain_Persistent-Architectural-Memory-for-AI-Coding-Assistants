package com.example.DevBrain.exception;

/**
 * Custom runtime exception representing SSL/TLS negotiation failures when communicating with external API services.
 */
public class TLSHandshakeException extends RuntimeException {
    
    public TLSHandshakeException(String message) {
        super(message);
    }

    public TLSHandshakeException(String message, Throwable cause) {
        super(message, cause);
    }
}
