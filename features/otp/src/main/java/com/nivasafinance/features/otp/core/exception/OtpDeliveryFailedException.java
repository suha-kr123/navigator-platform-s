package com.nivasafinance.features.otp.core.exception;

import com.nivasafinance.common.exception.BadRequestException;

public class OtpDeliveryFailedException extends BadRequestException {

    private static final long serialVersionUID = 1L;

    public OtpDeliveryFailedException(String message) {
        super(message);
    }
}
