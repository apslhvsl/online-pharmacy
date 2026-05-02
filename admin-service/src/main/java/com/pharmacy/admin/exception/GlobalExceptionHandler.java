package com.pharmacy.admin.exception;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @ExceptionHandler(FeignException.NotFound.class)
    public ResponseEntity<Map<String, Object>> handleFeignNotFound(
            FeignException.NotFound ex, WebRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "Resource not found", request);
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<Map<String, Object>> handleFeignException(
            FeignException ex, WebRequest request) {
        // Try to extract the clean error message from the downstream service response body
        String message = extractDownstreamMessage(ex);
        HttpStatus status = HttpStatus.resolve(ex.status());
        if (status == null || status.is5xxServerError()) {
            status = HttpStatus.BAD_GATEWAY;
        }
        return buildError(status, message, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(
            Exception ex, WebRequest request) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }

    /**
     * Parses the Feign response body to extract a human-readable message.
     * Downstream services return {"error": "..."} or {"message": "..."}.
     * Falls back to a generic message if parsing fails.
     */
    private String extractDownstreamMessage(FeignException ex) {
        try {
            byte[] body = ex.responseBody().map(buf -> {
                byte[] bytes = new byte[buf.remaining()];
                buf.get(bytes);
                return bytes;
            }).orElse(null);

            if (body != null && body.length > 0) {
                JsonNode node = MAPPER.readTree(body);
                // Order-service uses "error" field; auth/catalog use "message"
                for (String field : new String[]{"message", "error"}) {
                    JsonNode val = node.get(field);
                    if (val != null && val.isTextual() && !val.asText().isBlank()) {
                        return val.asText();
                    }
                }
            }
        } catch (Exception ignored) {
            // fall through to generic message
        }
        return "The requested operation could not be completed. Please try again.";
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