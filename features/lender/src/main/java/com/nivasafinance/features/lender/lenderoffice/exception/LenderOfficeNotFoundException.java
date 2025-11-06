package com.nivasafinance.features.lender.lenderoffice.exception;

import com.nivasafinance.common.exception.ResourceNotFoundException;

import java.io.Serial;

public class LenderOfficeNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    public LenderOfficeNotFoundException(String message) {
        super(message);
    }
}

