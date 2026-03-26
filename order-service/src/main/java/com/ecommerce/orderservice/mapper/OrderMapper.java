package com.ecommerce.orderservice.mapper;

import com.ecommerce.orderservice.dto.response.OrderItemResponse;
import com.ecommerce.orderservice.dto.response.OrderResponse;
import com.ecommerce.orderservice.entity.Order;
import com.ecommerce.orderservice.entity.OrderItem;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getUserId(),
                toItemResponses(order.getItems()),
                order.getTotalAmount(),
                order.getStatus().name(),
                order.getShippingAddress(),
                order.getNotes(),
                order.getCreatedAt()
        );
    }

    private List<OrderItemResponse> toItemResponses(List<OrderItem> items) {
        return items.stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getProductId(),
                        item.getProductName(),
                        item.getProductSku(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getTotalPrice()
                ))
                .toList();
    }
}