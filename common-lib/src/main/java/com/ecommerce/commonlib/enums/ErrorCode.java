package com.ecommerce.commonlib.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Generic
    INTERNAL_SERVER_ERROR("ERR-000", "Internal server error"),
    VALIDATION_FAILED("ERR-001", "Validation failed"),
    RESOURCE_NOT_FOUND("ERR-002", "Resource not found"),

    // User Service
    USER_NOT_FOUND("USR-001", "User not found"),
    USER_ALREADY_EXISTS("USR-002", "User already exists"),
    INVALID_CREDENTIALS("USR-003", "Invalid credentials"),

    // Order Service
    ORDER_NOT_FOUND("ORD-001", "Order not found"),
    ORDER_ALREADY_CANCELLED("ORD-002", "Order is already cancelled"),

    // Inventory Service
    INSUFFICIENT_STOCK("INV-001", "Insufficient stock available"),

    // Payment Service
    PAYMENT_FAILED("PAY-001", "Payment processing failed"),
    PAYMENT_NOT_FOUND("PAY-002", "Payment not found");

    private final String code;
    private final String defaultMessage;
}