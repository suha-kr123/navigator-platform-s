package com.nivasafinance.features.leadotp.exception;

import com.nivasafinance.common.exception.BadRequestException;

public class LeadOtpMissingPrimaryMobileException extends BadRequestException {

    private static final long serialVersionUID = 1L;

    public LeadOtpMissingPrimaryMobileException(String message) {
        super(message);
    }
}
