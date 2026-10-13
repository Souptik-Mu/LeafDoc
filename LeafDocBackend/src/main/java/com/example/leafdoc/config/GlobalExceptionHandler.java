package com.example.leafdoc.config;

import com.example.leafdoc.exceptions.InvalidCredentialsException;
import com.example.leafdoc.exceptions.MlServiceResponseException;
import com.example.leafdoc.exceptions.MlServiceUnavailableException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // valid check
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
    // AccessDeniedException  -> HttpStatus.FORBIDDEN //403
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
}
