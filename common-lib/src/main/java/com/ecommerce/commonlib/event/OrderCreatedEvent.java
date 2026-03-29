package com.ecommerce.commonlib.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderCreatedEvent(
        // Unique event ID — for idempotency on the consumer side
        String eventId,
        String orderNumber,
        Long orderId,
        Long userId,
        String userEmail,
        List<OrderItemEvent> items,
        BigDecimal totalAmount,
        String shippingAddress,
        LocalDateTime occurredAt
) {
    public static OrderCreatedEvent of(String orderNumber,
                                       Long orderId,
                                       Long userId,
                                       String userEmail,
                                       List<OrderItemEvent> items,
                                       BigDecimal totalAmount,
                                       String shippingAddress) {
        return new OrderCreatedEvent(
                java.util.UUID.randomUUID().toString(), // unique eventId
                orderNumber,
                orderId,
                userId,
                userEmail,
                items,
                totalAmount,
                shippingAddress,
                LocalDateTime.now()
        );
    }
}