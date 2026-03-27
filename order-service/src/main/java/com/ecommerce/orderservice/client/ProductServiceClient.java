package com.ecommerce.orderservice.client;

import com.ecommerce.commonlib.dto.ApiResponse;
import com.ecommerce.orderservice.client.dto.ProductResponse;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductServiceClient {

    private final WebClient.Builder webClientBuilder;
    private static final String PRODUCT_SERVICE_URL = "http://product-service";
    private static final String CB_NAME = "productServiceCB";
    private static final String BULKHEAD_NAME = "productServiceBulkhead";

    @CircuitBreaker(name = CB_NAME, fallbackMethod = "getProductByIdFallback")
    @Retry(name = "productServiceRetry")
    @Bulkhead(name = BULKHEAD_NAME)
    public Optional<ProductResponse> getProductById(Long productId) {
        log.info("Calling product-service for productId: {}", productId);

        ApiResponse<ProductResponse> response = webClientBuilder.build()
                .get()
                .uri(PRODUCT_SERVICE_URL + "/api/v1/products/{id}", productId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<ProductResponse>>() {})
                .block();

        return Optional.ofNullable(response).map(ApiResponse::data);
    }

    private Optional<ProductResponse> getProductByIdFallback(Long productId, Exception ex) {
        log.error("Fallback triggered for getProductById productId={} reason={}",
                productId, ex.getMessage());
        return Optional.empty();
    }
}