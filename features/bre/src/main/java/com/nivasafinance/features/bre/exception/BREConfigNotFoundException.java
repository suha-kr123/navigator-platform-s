package com.nivasafinance.features.bre.exception;

import com.nivasafinance.common.exception.ResourceNotFoundException;

import java.io.Serial;

public class BREConfigNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = -8123749823749826L;

    public BREConfigNotFoundException(String message) {
        super(message);
    }
}
