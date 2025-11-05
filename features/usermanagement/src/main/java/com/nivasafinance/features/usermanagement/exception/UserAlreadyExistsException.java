package com.nivasafinance.features.usermanagement.exception;

import java.io.Serial;

public class UserAlreadyExistsException extends RuntimeException {
    
    @Serial
    private static final long serialVersionUID = 1002L;
    
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}

