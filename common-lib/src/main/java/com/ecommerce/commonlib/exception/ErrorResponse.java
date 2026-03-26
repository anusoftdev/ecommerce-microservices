package com.ecommerce.commonlib.exception;

import java.time.LocalDateTime;

public record ErrorResponse(
        String errorCode,
        String message,
        Object details,
        LocalDateTime timestamp
) {
    // Convenience constructor without details
    public ErrorResponse(String errorCode, String message) {
        this(errorCode, message, null, LocalDateTime.now());
    }

    public ErrorResponse(String errorCode, String message, Object details) {
        this(errorCode, message, details, LocalDateTime.now());
    }
}