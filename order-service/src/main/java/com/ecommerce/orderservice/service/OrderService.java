package com.ecommerce.orderservice.service;

import com.ecommerce.commonlib.util.PageResponse;
import com.ecommerce.orderservice.dto.request.CreateOrderRequest;
import com.ecommerce.orderservice.dto.response.OrderResponse;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderResponse createOrder(CreateOrderRequest request);
    OrderResponse getOrderById(Long id);
    OrderResponse getOrderByNumber(String orderNumber);
    PageResponse<OrderResponse> getOrdersByUser(Long userId, Pageable pageable);
    OrderResponse cancelOrder(Long id);
}