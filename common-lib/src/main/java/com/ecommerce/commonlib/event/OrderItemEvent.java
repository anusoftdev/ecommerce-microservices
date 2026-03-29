package com.ecommerce.commonlib.event;

import java.math.BigDecimal;

public record OrderItemEvent(
        Long productId,
        String productName,
        String productSku,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice
) {}