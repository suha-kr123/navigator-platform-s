package com.nivasafinance.features.rolemanagement.exception;

import java.io.Serial;

public class RoleManagementNotFoundException extends RuntimeException {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    public RoleManagementNotFoundException(String message) {
        super(message);
    }
}

