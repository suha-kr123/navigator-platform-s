package com.nivasafinance.features.leadotp.exception;

import com.nivasafinance.common.exception.BadRequestException;

public class InvalidLeadOtpContactException extends BadRequestException {

    private static final long serialVersionUID = 1L;
    public InvalidLeadOtpContactException(String message) {
        super(message);
    }
}
