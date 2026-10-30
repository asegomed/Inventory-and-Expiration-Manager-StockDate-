package com.example.stockdate.presentation.controller;

import com.example.stockdate.presentation.dto.response.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleInvalidRequest(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiError(exception.getMessage()));
    }
}
