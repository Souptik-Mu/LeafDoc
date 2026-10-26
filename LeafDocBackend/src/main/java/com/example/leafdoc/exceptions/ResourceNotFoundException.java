package com.example.leafdoc.exceptions;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException() {
        super("Plant Does Not Exist for user");
    }
}
