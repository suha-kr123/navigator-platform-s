package com.nivasafinance.features.leadlender.exception;

import com.nivasafinance.common.exception.ResourceNotFoundException;

import java.io.Serial;

public class LeadNotFoundException extends ResourceNotFoundException {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    public LeadNotFoundException(String message) {
        super(message);
    }
}

