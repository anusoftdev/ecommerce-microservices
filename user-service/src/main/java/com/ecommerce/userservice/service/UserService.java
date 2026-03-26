package com.ecommerce.userservice.service;

import com.ecommerce.userservice.dto.request.CreateUserRequest;
import com.ecommerce.userservice.dto.response.UserResponse;
import java.util.List;

public interface UserService {
    UserResponse createUser(CreateUserRequest request);
    UserResponse getUserById(Long id);
    List<UserResponse> getAllUsers();
    void deleteUser(Long id);
}