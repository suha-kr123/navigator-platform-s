package com.nivasafinance.features.leadlender.exception;

import com.nivasafinance.common.exception.BadRequestException;

import java.io.Serial;

public class InvalidLenderException extends BadRequestException {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    public InvalidLenderException(String message) {
        super(message);
    }
}

