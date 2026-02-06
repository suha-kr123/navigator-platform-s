package com.nivasafinance.features.creditbureau.exception;

import com.nivasafinance.common.exception.ResourceNotFoundException;

import java.io.Serial;

public class CreditBureauNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = -8123749823749823L;

    public CreditBureauNotFoundException(String message) {
        super(message);
    }
}
