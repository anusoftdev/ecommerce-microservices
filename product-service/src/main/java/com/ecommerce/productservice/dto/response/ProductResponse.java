package com.ecommerce.productservice.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        String sku,
        String category,
        String status,
        LocalDateTime createdAt
) {}