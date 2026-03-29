package com.ecommerce.notificationservice.consumer;

import com.ecommerce.commonlib.event.KafkaTopics;
import com.ecommerce.commonlib.event.OrderCancelledEvent;
import com.ecommerce.commonlib.event.OrderCreatedEvent;
import com.ecommerce.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = KafkaTopics.ORDER_CREATED,
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOrderCreated(
            @Payload OrderCreatedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("Received ORDER_CREATED: eventId={} orderNumber={} " +
                        "topic={} partition={} offset={}",
                event.eventId(), event.orderNumber(),
                topic, partition, offset);

        // Idempotency check — eventId ensures we don't send
        // duplicate notifications if Kafka redelivers the message
        notificationService.sendOrderConfirmation(event);
    }

    @KafkaListener(
            topics = KafkaTopics.ORDER_CANCELLED,
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOrderCancelled(
            @Payload OrderCancelledEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("Received ORDER_CANCELLED: eventId={} orderNumber={} " +
                        "topic={} partition={} offset={}",
                event.eventId(), event.orderNumber(),
                topic, partition, offset);

        notificationService.sendOrderCancellationNotification(event);
    }
}