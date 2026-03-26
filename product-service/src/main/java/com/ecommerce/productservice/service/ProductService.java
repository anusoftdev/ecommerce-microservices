package com.ecommerce.productservice.service;

import com.ecommerce.commonlib.util.PageResponse;
import com.ecommerce.productservice.dto.request.CreateProductRequest;
import com.ecommerce.productservice.dto.request.UpdateProductRequest;
import com.ecommerce.productservice.dto.response.ProductResponse;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    ProductResponse createProduct(CreateProductRequest request);
    ProductResponse getProductById(Long id);
    ProductResponse getProductBySku(String sku);
    PageResponse<ProductResponse> getAllProducts(Pageable pageable);
    PageResponse<ProductResponse> getProductsByCategory(String category, Pageable pageable);
    ProductResponse updateProduct(Long id, UpdateProductRequest request);
    void deleteProduct(Long id);
}