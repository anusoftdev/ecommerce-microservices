package com.ecommerce.userservice.dto.response;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn,
        String email,
        String role
) {
    // Static factory — "Bearer" is always the token type for JWT
    public static AuthResponse of(String accessToken,
                                  String refreshToken,
                                  Long expiresIn,
                                  String email,
                                  String role) {
        return new AuthResponse(
                accessToken, refreshToken,
                "Bearer", expiresIn, email, role);
    }
}