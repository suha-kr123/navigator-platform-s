package com.nivasafinance.features.usermanagement.exception;

import java.io.Serial;

public class UserNotFoundException extends RuntimeException {
    
    @Serial
    private static final long serialVersionUID = 1001L;
    
    public UserNotFoundException(String message) {
        super(message);
    }
}

