package com.ecommerce.orderservice.event;

import com.ecommerce.commonlib.event.KafkaTopics;
import com.ecommerce.commonlib.event.OrderCancelledEvent;
import com.ecommerce.commonlib.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishOrderCreated(OrderCreatedEvent event) {
        // Key = orderNumber ensures all events for same order
        // go to the same partition — preserving order
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(
                        KafkaTopics.ORDER_CREATED,
                        event.orderNumber(),  // partition key
                        event
                );

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Published ORDER_CREATED: orderNumber={} partition={} offset={}",
                        event.orderNumber(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                // In production: store in outbox table for retry
                log.error("Failed to publish ORDER_CREATED: orderNumber={} error={}",
                        event.orderNumber(), ex.getMessage());
            }
        });
    }

    public void publishOrderCancelled(OrderCancelledEvent event) {
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(
                        KafkaTopics.ORDER_CANCELLED,
                        event.orderNumber(),
                        event
                );

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Published ORDER_CANCELLED: orderNumber={}",
                        event.orderNumber());
            } else {
                log.error("Failed to publish ORDER_CANCELLED: orderNumber={} error={}",
                        event.orderNumber(), ex.getMessage());
            }
        });
    }
}