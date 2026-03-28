package com.ecommerce.commonlib.security;

import io.jsonwebtoken.Claims;

public record TokenValidationResult(
        boolean valid,
        Claims claims,
        String failureReason
) {
    public static TokenValidationResult valid(Claims claims) {
        return new TokenValidationResult(true, claims, null);
    }

    public static TokenValidationResult invalid(String reason) {
        return new TokenValidationResult(false, null, reason);
    }
}