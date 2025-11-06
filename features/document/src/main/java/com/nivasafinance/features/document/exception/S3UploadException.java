package com.nivasafinance.features.document.exception;

import org.springframework.context.MessageSource;

import java.io.Serial;

public class S3UploadException extends S3OperationException {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    public static final String S3_UPLOAD_FAILED_KEY = "error.document.s3.upload.failed";
    
    public S3UploadException(String message) {
        super(message);
    }
    
    public S3UploadException(MessageSource messageSource) {
        super(S3_UPLOAD_FAILED_KEY, messageSource);
    }
    
    public S3UploadException(String error, MessageSource messageSource) {
        super(S3_UPLOAD_FAILED_KEY, error, messageSource);
    }
}

