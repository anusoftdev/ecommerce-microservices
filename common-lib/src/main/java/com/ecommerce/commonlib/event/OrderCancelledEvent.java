package com.ecommerce.commonlib.event;

import java.time.LocalDateTime;

public record OrderCancelledEvent(
        String eventId,
        String orderNumber,
        Long orderId,
        Long userId,
        String reason,
        LocalDateTime occurredAt
) {
    public static OrderCancelledEvent of(String orderNumber,
                                         Long orderId,
                                         Long userId,
                                         String reason) {
        return new OrderCancelledEvent(
                java.util.UUID.randomUUID().toString(),
                orderNumber,
                orderId,
                userId,
                reason,
                LocalDateTime.now()
        );
    }
}