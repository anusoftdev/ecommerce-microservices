package com.ecommerce.commonlib.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        Object errors,
        LocalDateTime timestamp
) {

    public ApiResponse {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }

    // ── Static factory helpers ──────────────────────────────────────────

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(
                true,
                "Operation successful",
                data,
                null,
                LocalDateTime.now()
        );
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(
                true,
                message,
                data,
                null,
                LocalDateTime.now()
        );
    }

    public static <T> ApiResponse<T> failure(String message) {
        return new ApiResponse<>(
                false,
                message,
                null,
                null,
                LocalDateTime.now()
        );
    }

    public static <T> ApiResponse<T> failure(String message, Object errors) {
        return new ApiResponse<>(
                false,
                message,
                null,
                errors,
                LocalDateTime.now()
        );
    }
}