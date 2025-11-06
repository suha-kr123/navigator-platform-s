package com.nivasafinance.features.document.exception;

import org.springframework.context.MessageSource;

import java.io.Serial;

public class S3DownloadException extends S3OperationException {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    public static final String S3_DOWNLOAD_FAILED_KEY = "error.document.s3.download.failed";
    
    public S3DownloadException(String message) {
        super(message);
    }
    
    public S3DownloadException(MessageSource messageSource) {
        super(S3_DOWNLOAD_FAILED_KEY, messageSource);
    }
    
    public S3DownloadException(String error, MessageSource messageSource) {
        super(S3_DOWNLOAD_FAILED_KEY, error, messageSource);
    }
}

