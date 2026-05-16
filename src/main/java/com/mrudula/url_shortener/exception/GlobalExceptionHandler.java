package com.mrudula.url_shortener.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleException(
            RuntimeException ex) {

        HttpStatus status = ex.getMessage().contains("expired")
                ? HttpStatus.GONE          // 410 for expired URLs
                : HttpStatus.NOT_FOUND;    // 404 for not found

        return ResponseEntity.status(status)
                .body(Map.of("error", ex.getMessage()));
    }
}
