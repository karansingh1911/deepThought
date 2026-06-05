package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessValidationException.class)
    public ResponseEntity<ErrorResponse> handleBusinessValidationException(BusinessValidationException ex) {

        ErrorResponse response = ErrorResponse.builder().error(ex.getErrorCode()).message(ex.getMessage()).timestamp(Instant.now()).build();

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {

        ErrorResponse response = ErrorResponse.builder().error("RESOURCE_NOT_FOUND").message(ex.getMessage()).timestamp(Instant.now()).build();

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {

        String message = ex.getBindingResult().getFieldErrors().stream().findFirst().map(fieldError -> fieldError.getDefaultMessage()).orElse("Validation failed");

        ErrorResponse response = ErrorResponse.builder().error("VALIDATION_ERROR").message(message).timestamp(Instant.now()).build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }


}