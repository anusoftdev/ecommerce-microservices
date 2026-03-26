package com.ecommerce.orderservice.exception;

import com.ecommerce.commonlib.enums.ErrorCode;
import com.ecommerce.commonlib.exception.ResourceNotFoundException;

public class OrderNotFoundException extends ResourceNotFoundException {

    public OrderNotFoundException(Long id) {
        super(ErrorCode.ORDER_NOT_FOUND, "Order not found with id: " + id);
    }

    public OrderNotFoundException(String orderNumber) {
        super(ErrorCode.ORDER_NOT_FOUND, "Order not found with number: " + orderNumber);
    }
}