package com.nivasafinance.features.leadlender.exception;

import com.nivasafinance.common.exception.ResourceNotFoundException;

import java.io.Serial;

public class LeadLenderNotFoundException extends ResourceNotFoundException {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    public LeadLenderNotFoundException(String message) {
        super(message);
    }
}

