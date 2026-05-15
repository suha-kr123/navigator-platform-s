package com.nivasafinance.features.leadotp.exception;

import com.nivasafinance.common.exception.BadRequestException;

public class LeadOtpActiveTokenNotFoundException extends BadRequestException {

    private static final long serialVersionUID = 1L;

    public LeadOtpActiveTokenNotFoundException(String message) {
        super(message);
    }
}
