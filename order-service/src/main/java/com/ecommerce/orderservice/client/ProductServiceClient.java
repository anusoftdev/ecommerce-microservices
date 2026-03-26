package com.ecommerce.orderservice.client;

import com.ecommerce.commonlib.dto.ApiResponse;
import com.ecommerce.orderservice.client.dto.ProductResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductServiceClient {

    private final WebClient.Builder webClientBuilder;

    private static final String PRODUCT_SERVICE_URL = "http://product-service";

    public Optional<ProductResponse> getProductById(Long productId) {
        try {
            ApiResponse<ProductResponse> response = webClientBuilder.build()
                    .get()
                    .uri(PRODUCT_SERVICE_URL + "/api/v1/products/{id}", productId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<ProductResponse>>() {})
                    .block();

            return Optional.ofNullable(response)
                    .map(ApiResponse::data);

        } catch (WebClientResponseException.NotFound e) {
            log.warn("Product not found with id: {}", productId);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error calling product-service for productId: {}", productId, e);
            return Optional.empty();
        }
    }
}