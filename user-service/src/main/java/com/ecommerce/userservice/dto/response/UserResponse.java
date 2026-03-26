package com.ecommerce.userservice.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String status,
        LocalDateTime createdAt
) {}
