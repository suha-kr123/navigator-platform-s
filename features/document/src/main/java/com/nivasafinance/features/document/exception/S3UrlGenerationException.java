package com.nivasafinance.features.document.exception;

import org.springframework.context.MessageSource;

import java.io.Serial;

public class S3UrlGenerationException extends S3OperationException {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    public static final String S3_URL_GENERATION_FAILED_KEY = "error.document.s3.url.generation.failed";
    
    public S3UrlGenerationException(String message) {
        super(message);
    }
    
    public S3UrlGenerationException(MessageSource messageSource) {
        super(S3_URL_GENERATION_FAILED_KEY, messageSource);
    }
    
    public S3UrlGenerationException(String error, MessageSource messageSource) {
        super(S3_URL_GENERATION_FAILED_KEY, error, messageSource);
    }
}

