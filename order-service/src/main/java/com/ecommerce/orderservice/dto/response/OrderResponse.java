package com.ecommerce.orderservice.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        String orderNumber,
        Long userId,
        List<OrderItemResponse> items,
        BigDecimal totalAmount,
        String status,
        String shippingAddress,
        String notes,
        LocalDateTime createdAt
) {}