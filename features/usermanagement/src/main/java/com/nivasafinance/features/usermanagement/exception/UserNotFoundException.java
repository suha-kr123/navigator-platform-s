package com.nivasafinance.features.usermanagement.exception;

import com.nivasafinance.common.exception.ResourceNotFoundException;

import java.io.Serial;

public class UserNotFoundException extends ResourceNotFoundException {
    
    @Serial
    private static final long serialVersionUID = 1001L;
    
    public UserNotFoundException(String message) {
        super(message);
    }
}

