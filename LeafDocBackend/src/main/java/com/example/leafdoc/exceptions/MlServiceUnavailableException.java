package com.example.leafdoc.exceptions;

public class MlServiceUnavailableException extends RuntimeException {
    public MlServiceUnavailableException() {
        super("The ML service is temporarily unavailable");
    }
    public MlServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
