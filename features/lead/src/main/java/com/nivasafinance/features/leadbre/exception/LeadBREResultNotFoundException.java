package com.nivasafinance.features.leadbre.exception;

import com.nivasafinance.common.exception.ResourceNotFoundException;

import java.io.Serial;

public class LeadBREResultNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 7293847562938475623L;

    public LeadBREResultNotFoundException(String message) {
        super(message);
    }
}
