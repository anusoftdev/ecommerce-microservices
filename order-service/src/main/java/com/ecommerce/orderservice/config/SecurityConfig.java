package com.ecommerce.orderservice.config;

import com.ecommerce.commonlib.security.BaseSecurityConfig;
import com.ecommerce.commonlib.security.JwtTokenValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig extends BaseSecurityConfig {

    public SecurityConfig(
            @Value("${application.security.jwt.secret-key}") String secretKey) {
        super(new JwtTokenValidator(secretKey));
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {
        // No public endpoints in order-service
        return buildSecurityFilterChain(http);
    }
}