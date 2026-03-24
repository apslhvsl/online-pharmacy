package com.pharmacy.admin.exception;

import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FeignException.NotFound.class)
    public ResponseEntity<Map<String, Object>> handleFeignNotFound(
            FeignException.NotFound ex, WebRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "Resource not found", request);
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<Map<String, Object>> handleFeignException(
            FeignException ex, WebRequest request) {
        return buildError(HttpStatus.BAD_GATEWAY, "Upstream service error: " + ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(
            Exception ex, WebRequest request) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }

    private ResponseEntity<Map<String, Object>> buildError(
            HttpStatus status, String message, WebRequest request) {
        return ResponseEntity.status(status).body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message,
                "path", request.getDescription(false).replace("uri=", "")
        ));
    }
}