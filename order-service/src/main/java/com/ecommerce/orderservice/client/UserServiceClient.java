package com.ecommerce.orderservice.client;

import com.ecommerce.commonlib.dto.ApiResponse;
import com.ecommerce.orderservice.client.dto.UserResponse;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
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
public class UserServiceClient {

    private final WebClient.Builder webClientBuilder;
    private static final String USER_SERVICE_URL = "http://user-service";
    private static final String CB_NAME = "userServiceCB";
    private static final String BULKHEAD_NAME = "userServiceBulkhead";

    @CircuitBreaker(name = CB_NAME, fallbackMethod = "getUserByIdFallback")
    @Retry(name = "userServiceRetry")
    @Bulkhead(name = BULKHEAD_NAME)
    public Optional<UserResponse> getUserById(Long userId) {
        log.info("Calling user-service for userId: {}", userId);

        ApiResponse<UserResponse> response = webClientBuilder.build()
                .get()
                .uri(USER_SERVICE_URL + "/api/v1/users/{id}", userId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<UserResponse>>() {})
                .block();

        return Optional.ofNullable(response).map(ApiResponse::data);
    }

    // Fallback — called when circuit is OPEN or all retries exhausted
    private Optional<UserResponse> getUserByIdFallback(Long userId, Exception ex) {
        log.error("Fallback triggered for getUserById userId={} reason={}",
                userId, ex.getMessage());

        // Return empty — caller (OrderService) will throw BusinessException
        return Optional.empty();
    }
}