package com.nivasafinance.features.leadlender.exception;

import com.nivasafinance.common.exception.BadRequestException;

import java.io.Serial;

public class LeadLenderAlreadyExistsException extends BadRequestException {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    public LeadLenderAlreadyExistsException(String message) {
        super(message);
    }
}

