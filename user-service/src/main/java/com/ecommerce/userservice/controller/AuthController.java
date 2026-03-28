package com.ecommerce.userservice.controller;

import com.ecommerce.commonlib.dto.ApiResponse;
import com.ecommerce.userservice.dto.request.CreateUserRequest;
import com.ecommerce.userservice.dto.request.LoginRequest;
import com.ecommerce.userservice.dto.response.AuthResponse;
import com.ecommerce.userservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Registered successfully",
                authService.register(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Login successful",
                authService.login(request)));
    }
}