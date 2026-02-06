package com.nivasafinance.features.consent.exception;

import com.nivasafinance.common.exception.ResourceNotFoundException;

import java.io.Serial;

public class ConsentNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ConsentNotFoundException(String message) {
        super(message);
    }
}
