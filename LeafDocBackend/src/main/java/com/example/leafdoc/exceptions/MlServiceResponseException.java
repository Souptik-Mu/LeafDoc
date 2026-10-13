package com.example.leafdoc.exceptions;

import lombok.Getter;

@Getter
public class MlServiceResponseException extends RuntimeException {
    public MlServiceResponseException(String message) {
        super(message);
    }
}
