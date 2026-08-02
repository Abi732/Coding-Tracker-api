package com.abi.coding_tracker.exception;


import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.abi.coding_tracker.dto.ErrorResponse;
import com.abi.coding_tracker.dto.ValidationErrorResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex){
        log.warn("Resource Not Found: {}", ex.getMessage());
        ErrorResponse errorResponse =new ErrorResponse(
            LocalDateTime.now(), 
            HttpStatus.NOT_FOUND.value(), 
            ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResourceException(DuplicateResourceException ex){
        log.warn("Conflict occured : {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(), 
            HttpStatus.CONFLICT.value(), 
            ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(MethodArgumentNotValidException ex){
        log.warn("validation failed for incoming request");

        Map<String, String> fieldError = new HashMap<>();
        for(FieldError error : ex.getBindingResult().getFieldErrors()){
            fieldError.put(error.getField(), error.getDefaultMessage());
        }

        ValidationErrorResponse validationErrorResponse = new ValidationErrorResponse(
            LocalDateTime.now(), 
            HttpStatus.BAD_REQUEST.value(),
            fieldError);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationErrorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex){
        log.error("An unexpected error occurred: ", ex);
        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(), 
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<ErrorResponse> handleExternalApiException(ExternalApiException ex){
        log.error("External API Error : {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(), 
            ex.getStatusCode(), 
            ex.getMessage()
        );

        return ResponseEntity.status(ex.getStatusCode()).body(errorResponse);
    }
}
