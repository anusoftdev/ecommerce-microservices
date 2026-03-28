package com.ecommerce.orderservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Slf4j
public class WebClientConfig {

    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
                .filter((request, next) -> {

                    // Get current authentication from SecurityContext
                    // This is set by JwtAuthenticationFilter on each request
                    Authentication authentication =
                            SecurityContextHolder.getContext().getAuthentication();

                    // credentials = raw JWT token
                    // (we stored it there in JwtAuthenticationFilter)
                    if (authentication != null
                            && authentication.getCredentials() instanceof String rawToken
                            && !rawToken.isBlank()) {

                        log.debug("Propagating JWT to downstream service: {}",
                                request.url().getHost());

                        // Build a new request with Authorization header added
                        ClientRequest authorizedRequest = ClientRequest
                                .from(request)
                                .header("Authorization", "Bearer " + rawToken)
                                .build();

                        return next.exchange(authorizedRequest);
                    }

                    // No token in context — forward request as-is
                    // downstream service will reject if endpoint is protected
                    return next.exchange(request);
                });
    }
}