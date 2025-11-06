package com.nivasafinance.features.document.exception;

import org.springframework.context.MessageSource;

import java.io.Serial;

public class S3ExpirationException extends S3OperationException {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    public static final String S3_EXPIRATION_INVALID_KEY = "error.document.s3.expiration.invalid";
    
    public S3ExpirationException(String message) {
        super(message);
    }
    
    public S3ExpirationException(MessageSource messageSource) {
        super(S3_EXPIRATION_INVALID_KEY, messageSource);
    }
}

