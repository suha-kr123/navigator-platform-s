package com.nivasafinance.features.otp.core.exception;

import com.nivasafinance.common.exception.BadRequestException;

public class OtpValidationException extends BadRequestException {

    private static final long serialVersionUID = 1L;

    public OtpValidationException(String message) {
        super(message);
    }
}
