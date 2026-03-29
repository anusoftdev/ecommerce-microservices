package com.ecommerce.notificationservice.service.impl;

import com.ecommerce.commonlib.event.OrderCancelledEvent;
import com.ecommerce.commonlib.event.OrderCreatedEvent;
import com.ecommerce.notificationservice.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    @Override
    public void sendOrderConfirmation(OrderCreatedEvent event) {
        // In production: integrate with email provider (SendGrid, AWS SES)
        log.info("""
                ══════════════════════════════════════════
                📧 ORDER CONFIRMATION EMAIL
                ══════════════════════════════════════════
                To      : {}
                Subject : Order Confirmed - {}
                Body    : Hi! Your order {} has been placed.
                          Total: ₹{}
                          Items: {} item(s)
                          Ship to: {}
                ══════════════════════════════════════════
                """,
                event.userEmail(),
                event.orderNumber(),
                event.orderNumber(),
                event.totalAmount(),
                event.items().size(),
                event.shippingAddress()
        );
    }

    @Override
    public void sendOrderCancellationNotification(OrderCancelledEvent event) {
        log.info("""
                ══════════════════════════════════════════
                📧 ORDER CANCELLATION EMAIL
                ══════════════════════════════════════════
                To      : userId={}
                Subject : Order Cancelled - {}
                Body    : Your order {} has been cancelled.
                          Reason: {}
                ══════════════════════════════════════════
                """,
                event.userId(),
                event.orderNumber(),
                event.orderNumber(),
                event.reason()
        );
    }
}