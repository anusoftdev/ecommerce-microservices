package com.ecommerce.commonlib.exception;

import java.time.LocalDateTime;

public record ErrorResponse(
        String errorCode,
        String message,
        Object details,
        LocalDateTime timestamp
) {
    public static ErrorResponse of(String errorCode, String message) {
        return new ErrorResponse(errorCode, message, null, LocalDateTime.now());
    }

    public static ErrorResponse of(String errorCode, String message, Object details) {
        return new ErrorResponse(errorCode, message, details, LocalDateTime.now());
    }
}