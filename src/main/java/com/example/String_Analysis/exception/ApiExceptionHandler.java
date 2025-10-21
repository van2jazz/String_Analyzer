package com.example.String_Analysis.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(Exception ex) {
        // Generic fallback
        ErrorResponse er = new ErrorResponse(ex.getMessage() == null ? "error" : ex.getMessage());
        return ResponseEntity.badRequest().body(er);
    }
}
