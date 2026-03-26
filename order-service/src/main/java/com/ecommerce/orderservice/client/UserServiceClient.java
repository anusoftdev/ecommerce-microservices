package com.ecommerce.orderservice.client;

import com.ecommerce.commonlib.dto.ApiResponse;
import com.ecommerce.orderservice.client.dto.UserResponse;
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

    public Optional<UserResponse> getUserById(Long userId) {
        try {
            ApiResponse<UserResponse> response = webClientBuilder.build()
                    .get()
                    .uri(USER_SERVICE_URL + "/api/v1/users/{id}", userId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<UserResponse>>() {})
                    .block(); // blocking — we're in a servlet (MVC) context

            return Optional.ofNullable(response)
                    .map(ApiResponse::data);

        } catch (WebClientResponseException.NotFound e) {
            log.warn("User not found with id: {}", userId);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error calling user-service for userId: {}", userId, e);
            return Optional.empty();
        }
    }
}