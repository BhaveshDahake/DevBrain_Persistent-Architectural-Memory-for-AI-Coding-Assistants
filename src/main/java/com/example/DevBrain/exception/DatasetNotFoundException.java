package com.example.DevBrain.exception;

public class DatasetNotFoundException extends RuntimeException {
    public DatasetNotFoundException(String message) {
        super(message);
    }
}
