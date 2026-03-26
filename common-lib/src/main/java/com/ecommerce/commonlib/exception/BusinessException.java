package com.ecommerce.commonlib.exception;

import com.ecommerce.commonlib.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class BusinessException extends BaseException {

    public BusinessException(ErrorCode errorCode) {
        super(errorCode, HttpStatus.BAD_REQUEST);
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(errorCode, HttpStatus.BAD_REQUEST, message);
    }
}