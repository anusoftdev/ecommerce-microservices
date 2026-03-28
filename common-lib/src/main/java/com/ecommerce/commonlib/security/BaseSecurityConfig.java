package com.ecommerce.commonlib.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
public abstract class BaseSecurityConfig {

    // Each service provides its own instance of JwtTokenValidator
    // configured with its application.yml secret key
    protected final JwtTokenValidator jwtTokenValidator;

    /**
     * Called by each service's @Bean securityFilterChain method.
     * publicPaths = endpoints that don't require a JWT (e.g. /auth/**)
     */
    protected SecurityFilterChain buildSecurityFilterChain(
            HttpSecurity http,
            String... publicPaths) throws Exception {

        // Create filter using this service's validator instance
        JwtAuthenticationFilter jwtFilter =
                new JwtAuthenticationFilter(jwtTokenValidator);

        http
                // Disable CSRF — stateless APIs don't need it
                // (CSRF protects browser cookie sessions, not JWT)
                .csrf(csrf -> csrf.disable())

                // No sessions — every request must carry a JWT
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> {
                    // Register service-specific public paths first
                    for (String path : publicPaths) {
                        auth.requestMatchers(path).permitAll();
                    }
                    // Actuator always public — for health checks
                    auth.requestMatchers("/actuator/**").permitAll();
                    // Everything else requires valid JWT
                    auth.anyRequest().authenticated();
                })

                // JWT filter runs before Spring's username/password filter
                .addFilterBefore(jwtFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}