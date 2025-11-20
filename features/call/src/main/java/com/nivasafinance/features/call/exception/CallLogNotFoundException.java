package com.nivasafinance.features.call.exception;

import com.nivasafinance.common.exception.ResourceNotFoundException;

import java.io.Serial;

public class CallLogNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = -8123749823749823L;

    public CallLogNotFoundException(String message) {
        super(message);
    }
}


