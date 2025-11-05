package com.nivasafinance.features.usermanagement.exception;

import java.io.Serial;

public class UserOperationException extends RuntimeException {
    
    @Serial
    private static final long serialVersionUID = 1003L;
    
    public UserOperationException(String message) {
        super(message);
    }

    public UserOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}

