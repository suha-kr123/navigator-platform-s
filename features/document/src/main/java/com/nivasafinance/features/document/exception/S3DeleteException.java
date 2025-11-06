package com.nivasafinance.features.document.exception;

import org.springframework.context.MessageSource;

import java.io.Serial;

public class S3DeleteException extends S3OperationException {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    public static final String S3_DELETE_FAILED_KEY = "error.document.s3.delete.failed";
    
    public S3DeleteException(String message) {
        super(message);
    }
    
    public S3DeleteException(MessageSource messageSource) {
        super(S3_DELETE_FAILED_KEY, messageSource);
    }
    
    public S3DeleteException(String error, MessageSource messageSource) {
        super(S3_DELETE_FAILED_KEY, error, messageSource);
    }
}

