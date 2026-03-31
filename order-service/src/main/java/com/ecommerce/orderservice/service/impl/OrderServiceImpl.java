package com.ecommerce.orderservice.service.impl;

import com.ecommerce.commonlib.enums.ErrorCode;
import com.ecommerce.commonlib.event.OrderCancelledEvent;
import com.ecommerce.commonlib.event.OrderCreatedEvent;
import com.ecommerce.commonlib.event.OrderItemEvent;
import com.ecommerce.commonlib.exception.BusinessException;
import com.ecommerce.commonlib.util.PageResponse;
import com.ecommerce.orderservice.client.ProductServiceClient;
import com.ecommerce.orderservice.client.UserServiceClient;
import com.ecommerce.orderservice.client.dto.ProductResponse;
import com.ecommerce.orderservice.client.dto.UserResponse;
import com.ecommerce.orderservice.dto.request.CreateOrderRequest;
import com.ecommerce.orderservice.dto.request.OrderItemRequest;
import com.ecommerce.orderservice.dto.response.OrderResponse;
import com.ecommerce.orderservice.entity.Order;
import com.ecommerce.orderservice.entity.OrderItem;
import com.ecommerce.orderservice.event.OrderEventPublisher;
import com.ecommerce.orderservice.exception.OrderNotFoundException;
import com.ecommerce.orderservice.mapper.OrderMapper;
import com.ecommerce.orderservice.metrics.OrderMetrics;
import com.ecommerce.orderservice.repository.OrderRepository;
import com.ecommerce.orderservice.service.OrderService;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final UserServiceClient userServiceClient;
    private final ProductServiceClient productServiceClient;
    private final OrderEventPublisher eventPublisher;
    private final OrderMetrics orderMetrics;

    @Override
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Creating order for userId: {}", request.userId());
        Timer.Sample timerSample = orderMetrics.startOrderTimer();

        // 1. Validate user exists
        UserResponse user = userServiceClient.getUserById(request.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.VALIDATION_FAILED,
                        "User not found with id: " + request.userId()));

        log.info("Validated user: {} {}", user.firstName(), user.lastName());

        // 2. Build order items — validate each product
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.items()) {
            ProductResponse product = productServiceClient
                    .getProductById(itemRequest.productId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.VALIDATION_FAILED,
                            "Product not found with id: " + itemRequest.productId()));

            BigDecimal itemTotal = product.price()
                    .multiply(BigDecimal.valueOf(itemRequest.quantity()));

            OrderItem item = OrderItem.builder()
                    .productId(product.id())
                    .productName(product.name())
                    .productSku(product.sku())
                    .quantity(itemRequest.quantity())
                    .unitPrice(product.price())
                    .totalPrice(itemTotal)
                    .build();

            orderItems.add(item);
            totalAmount = totalAmount.add(itemTotal);
        }

        // 3. Build and save order
        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .userId(request.userId())
                .totalAmount(totalAmount)
                .status(Order.OrderStatus.PENDING)
                .shippingAddress(request.shippingAddress())
                .notes(request.notes())
                .build();

        orderItems.forEach(order::addItem);
        Order saved = orderRepository.save(order);

        log.info("Order created: {} for user: {}", saved.getOrderNumber(), request.userId());

        List<OrderItemEvent> eventItems = saved.getItems().stream()
                .map(item -> new OrderItemEvent(
                        item.getProductId(),
                        item.getProductName(),
                        item.getProductSku(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getTotalPrice()
                ))
                .toList();

        OrderCreatedEvent event = OrderCreatedEvent.of(
                saved.getOrderNumber(),
                saved.getId(),
                saved.getUserId(),
                user.email(),          // from the UserResponse we already fetched
                eventItems,
                saved.getTotalAmount(),
                saved.getShippingAddress()
        );

        eventPublisher.publishOrderCreated(event);
        log.info("Published ORDER_CREATED event for: {}", saved.getOrderNumber());

        orderMetrics.recordOrderCreated(saved.getTotalAmount());
        orderMetrics.stopOrderTimer(timerSample);

        return orderMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        return orderRepository.findById(id)
                .map(orderMapper::toResponse)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .map(orderMapper::toResponse)
                .orElseThrow(() -> new OrderNotFoundException(orderNumber));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getOrdersByUser(Long userId, Pageable pageable) {
        return PageResponse.from(
                orderRepository.findByUserId(userId, pageable)
                        .map(orderMapper::toResponse)
        );
    }

    @Override
    public OrderResponse cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        if (order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.ORDER_ALREADY_CANCELLED,
                    "Order is already cancelled: " + id);
        }

        if (order.getStatus() == Order.OrderStatus.DELIVERED) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED,
                    "Cannot cancel a delivered order");
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        Order cancelOrder = orderRepository.save(order);

        orderMetrics.recordOrderCancelled();

        OrderCancelledEvent cancelEvent = OrderCancelledEvent.of(
                cancelOrder.getOrderNumber(),
                cancelOrder.getId(),
                cancelOrder.getUserId(),
                "Cancelled by user"
        );
        eventPublisher.publishOrderCancelled(cancelEvent);
        log.info("Published ORDER_CANCELLED event for: {}", cancelOrder.getOrderNumber());
        return orderMapper.toResponse(cancelOrder);
    }

    private String generateOrderNumber() {
        return "ORD-" + Instant.now().toEpochMilli();
    }
}