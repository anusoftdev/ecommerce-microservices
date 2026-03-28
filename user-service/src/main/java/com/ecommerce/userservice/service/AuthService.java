package com.ecommerce.userservice.service;

import com.ecommerce.userservice.dto.request.CreateUserRequest;
import com.ecommerce.userservice.dto.request.LoginRequest;
import com.ecommerce.userservice.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(CreateUserRequest request);
    AuthResponse login(LoginRequest request);
}