package com.ecommerce.notificationservice.service;

import com.ecommerce.commonlib.event.OrderCancelledEvent;
import com.ecommerce.commonlib.event.OrderCreatedEvent;

public interface NotificationService {
    void sendOrderConfirmation(OrderCreatedEvent event);
    void sendOrderCancellationNotification(OrderCancelledEvent event);
}