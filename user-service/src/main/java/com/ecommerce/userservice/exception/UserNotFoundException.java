package com.ecommerce.userservice.exception;

import com.ecommerce.commonlib.enums.ErrorCode;
import com.ecommerce.commonlib.exception.ResourceNotFoundException;

public class UserNotFoundException extends ResourceNotFoundException {

    public UserNotFoundException(Long id) {
        super(ErrorCode.USER_NOT_FOUND, "User not found with id: " + id);
    }

    public UserNotFoundException(String email) {
        super(ErrorCode.USER_NOT_FOUND, "User not found with email: " + email);
    }
}