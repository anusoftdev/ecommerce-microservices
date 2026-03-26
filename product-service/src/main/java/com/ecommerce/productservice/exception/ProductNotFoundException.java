package com.ecommerce.productservice.exception;

import com.ecommerce.commonlib.enums.ErrorCode;
import com.ecommerce.commonlib.exception.ResourceNotFoundException;

public class ProductNotFoundException extends ResourceNotFoundException {

    public ProductNotFoundException(Long id) {
        super(ErrorCode.RESOURCE_NOT_FOUND, "Product not found with id: " + id);
    }

    public ProductNotFoundException(String sku) {
        super(ErrorCode.RESOURCE_NOT_FOUND, "Product not found with SKU: " + sku);
    }
}