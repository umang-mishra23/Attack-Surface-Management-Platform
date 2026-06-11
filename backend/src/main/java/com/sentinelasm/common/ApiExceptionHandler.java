package com.sentinelasm.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    ResponseEntity<Map<String, Object>> badRequest(RuntimeException error) {
        return ResponseEntity.badRequest().body(body(error.getMessage(), 400));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException error) {
        return ResponseEntity.badRequest().body(body("Validation failed", 400));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Map<String, Object>> server(Exception error) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body("Unexpected server error", 500));
    }

    private Map<String, Object> body(String message, int status) {
        return Map.of("timestamp", Instant.now(), "status", status, "error", message);
    }
}
