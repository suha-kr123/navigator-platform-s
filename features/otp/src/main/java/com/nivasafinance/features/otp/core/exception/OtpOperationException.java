package com.nivasafinance.features.otp.core.exception;

public class OtpOperationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public OtpOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
