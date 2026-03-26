package com.ecommerce.orderservice.client.dto;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String status
) {}