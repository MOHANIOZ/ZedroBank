package com.bankApp.banking_app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice // Acts as a global interceptor for exceptions thrown across the entire application
public class GlobalExceptionHandler {

    /**
     * Handles cases where a specific account is not found in the database.
     * Returns a structured JSON response with a 404 Not Found status.
     */
    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<Object> handleAccountNotFound(AccountNotFoundException ex) {

        // Constructing a custom structured error response body
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", ex.getMessage()); // Retrieves the custom message defined in the Service layer
        body.put("status", HttpStatus.NOT_FOUND.value());

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    /**
     * Generic handler for RuntimeExceptions (e.g., validation errors or business logic failures).
     * Returns the exception message with a 400 Bad Request status.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException exception) {
        // Returns the raw exception message directly to the client
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }
}