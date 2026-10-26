package com.example.leafdoc.config;

import com.example.leafdoc.exceptions.InvalidCredentialsException;
import com.example.leafdoc.exceptions.MlServiceResponseException;
import com.example.leafdoc.exceptions.MlServiceUnavailableException;
import com.example.leafdoc.exceptions.ResourceNotFoundException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /// validation check -> 400 BAD_REQUEST
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(
            MethodArgumentNotValidException ex
    ) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse("Invalid request");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST) //400
                .body(Map.of("error", message));
    }

    /// Invalid Credentials (authentication) -> 401 UNAUTHORISED
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleInvalidCredentials(
            InvalidCredentialsException ex
    ) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED) //401
                .body(Map.of(
                        "error", ex.getMessage()
                ));
    }

    // Access DeniedException  -> HttpStatus.FORBIDDEN //403

    /// Resource not found -> 404 NOT_FOUND
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleResourceNotFound(
            ResourceNotFoundException ex
    ){
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "error", "RESOURCE_NOT_FOUND",
                        "message", ex.getMessage()
                ));
    }

    /// Ml Service Error -> 502 BAD_GATEWAY
    @ExceptionHandler(MlServiceResponseException.class)
    public ResponseEntity<Map<String, String>> handleMlServiceResponseException(
            MlServiceResponseException ex
    ){
        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(Map.of(
                        "error", "ML_SERVICE_ERROR",
                        "message", "The ML service returned an unexpected response",
                        "status", ex.getMessage()
                ));
    }

    /// Ml Service Unavailable -> 503 SERVICE_UNAVAILABLE
    @ExceptionHandler(MlServiceUnavailableException.class)
    public ResponseEntity<Map<String, String>> handleMlServiceUnavailable(
            MlServiceUnavailableException ex
    ) {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "ML_SERVICE_UNAVAILABLE",
                        "message", ex.getMessage()
                ));
    }
}
