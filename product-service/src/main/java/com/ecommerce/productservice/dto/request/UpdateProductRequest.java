package com.ecommerce.productservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

public record UpdateProductRequest(

        String name,
        String description,

        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        BigDecimal price,

        String category,
        String status
) {}