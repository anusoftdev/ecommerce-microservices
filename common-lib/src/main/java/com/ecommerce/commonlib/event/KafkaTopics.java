package com.ecommerce.commonlib.event;

/**
 * Single source of truth for all Kafka topic names.
 * Both producers and consumers reference these constants.
 * Never use raw strings for topic names.
 */
public final class KafkaTopics {

    private KafkaTopics() {}

    public static final String ORDER_CREATED    = "order.created";
    public static final String ORDER_CANCELLED  = "order.cancelled";
    public static final String STOCK_RESERVED   = "inventory.stock.reserved";
    public static final String STOCK_FAILED     = "inventory.stock.reservation.failed";
}