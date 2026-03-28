package com.ecommerce.userservice.config;

import com.ecommerce.commonlib.security.BaseSecurityConfig;
import com.ecommerce.commonlib.security.JwtTokenValidator;
import com.ecommerce.userservice.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity

// Enables @PreAuthorize, @PostAuthorize on methods
// e.g. @PreAuthorize("hasRole('ADMIN')")
@EnableMethodSecurity
public class SecurityConfig extends BaseSecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;

    public SecurityConfig(
            @Value("${application.security.jwt.secret-key}") String secretKey,
            UserDetailsServiceImpl userDetailsService) {
        // Pass validator to BaseSecurityConfig
        super(new JwtTokenValidator(secretKey));
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {
        // Auth endpoints are public — everything else needs JWT
        return buildSecurityFilterChain(http,
                "/api/v1/auth/**"   // register, login — no token needed
        );
    }

    /**
     * AuthenticationProvider — wires Spring Security's auth
     * to our DB-backed UserDetailsService and BCrypt encoder.
     * Used by AuthenticationManager during login.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * AuthenticationManager — the entry point for credential
     * validation. Injected into AuthServiceImpl for login.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * BCrypt — industry standard for password hashing.
     * Never store plain text passwords.
     * Cost factor default (10) = ~100ms per hash — slow enough
     * to be brute-force resistant.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}