package com.ecommerce.orderservice.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Slf4j
public class OrderMetrics {

    // Counters — monotonically increasing
    private final Counter ordersCreatedCounter;
    private final Counter ordersCancelledCounter;
    private final Counter ordersFailedCounter;

    // Timer — tracks latency distribution
    private final Timer orderCreationTimer;

    // Gauge backing value — tracks current revenue
    private final AtomicLong totalRevenueInPaise;

    public OrderMetrics(MeterRegistry registry) {
        // Counter — increments on each order created
        this.ordersCreatedCounter = Counter.builder("orders.created.total")
                .description("Total number of orders created")
                .tag("service", "order-service")
                .register(registry);

        this.ordersCancelledCounter = Counter.builder("orders.cancelled.total")
                .description("Total number of orders cancelled")
                .tag("service", "order-service")
                .register(registry);

        this.ordersFailedCounter = Counter.builder("orders.failed.total")
                .description("Total number of failed order attempts")
                .tag("service", "order-service")
                .register(registry);

        // Timer — use to measure order creation duration
        this.orderCreationTimer = Timer.builder("orders.creation.duration")
                .description("Time taken to create an order")
                .tag("service", "order-service")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);

        // Gauge — current snapshot value (total revenue)
        this.totalRevenueInPaise = new AtomicLong(0);
        io.micrometer.core.instrument.Gauge
                .builder("orders.revenue.total",
                        totalRevenueInPaise, AtomicLong::get)
                .description("Total revenue in paise (1 INR = 100 paise)")
                .tag("service", "order-service")
                .register(registry);
    }

    public void recordOrderCreated(BigDecimal amount) {
        ordersCreatedCounter.increment();
        // Store as paise (integer) — avoids floating point issues in metrics
        totalRevenueInPaise.addAndGet(
                amount.multiply(BigDecimal.valueOf(100)).longValue());
    }

    public void recordOrderCancelled() {
        ordersCancelledCounter.increment();
    }

    public void recordOrderFailed() {
        ordersFailedCounter.increment();
    }

    public Timer.Sample startOrderTimer() {
        return Timer.start();
    }

    public void stopOrderTimer(Timer.Sample sample) {
        sample.stop(orderCreationTimer);
    }
}