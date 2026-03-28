package com.ecommerce.userservice.service.impl;

import com.ecommerce.commonlib.enums.ErrorCode;
import com.ecommerce.commonlib.exception.BusinessException;
import com.ecommerce.userservice.dto.request.CreateUserRequest;
import com.ecommerce.userservice.dto.request.LoginRequest;
import com.ecommerce.userservice.dto.response.AuthResponse;
import com.ecommerce.userservice.entity.User;
import com.ecommerce.userservice.repository.UserRepository;
import com.ecommerce.userservice.security.JwtService;
import com.ecommerce.userservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponse register(CreateUserRequest request) {
        // Check for duplicate email
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS,
                    "Email already registered: " + request.email());
        }

        // Build and save user with hashed password and default ROLE_USER
        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                // BCrypt hash — never store plain text
                .password(passwordEncoder.encode(request.password()))
                .status(User.UserStatus.ACTIVE)
                .role(User.Role.ROLE_USER)
                .build();

        User saved = userRepository.save(user);
        log.info("User registered: {}", saved.getEmail());

        // Issue JWT immediately after registration — user is logged in
        String accessToken = jwtService.generateAccessToken(
                saved.getEmail(), saved.getId(), saved.getRole().name());
        String refreshToken = jwtService.generateRefreshToken(saved.getEmail());

        return AuthResponse.of(accessToken, refreshToken,
                jwtService.getJwtExpiration(),
                saved.getEmail(), saved.getRole().name());
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        try {
            // AuthenticationManager validates credentials using
            // UserDetailsServiceImpl + BCryptPasswordEncoder
            // Throws BadCredentialsException if wrong
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(), request.password())
            );
        } catch (BadCredentialsException e) {
            // Always use a generic message — never reveal
            // whether the email or password was wrong
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS,
                    "Invalid email or password");
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.USER_NOT_FOUND, "User not found"));

        String accessToken = jwtService.generateAccessToken(
                user.getEmail(), user.getId(), user.getRole().name());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        log.info("User logged in: {}", user.getEmail());

        return AuthResponse.of(accessToken, refreshToken,
                jwtService.getJwtExpiration(),
                user.getEmail(), user.getRole().name());
    }
}