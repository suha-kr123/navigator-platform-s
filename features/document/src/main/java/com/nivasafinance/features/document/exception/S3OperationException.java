package com.nivasafinance.features.document.exception;

import com.nivasafinance.common.exception.BadRequestException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.io.Serial;
import java.util.Locale;

public class S3OperationException extends BadRequestException {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    private static final Locale locale = LocaleContextHolder.getLocale();
    public static final String S3_UPLOAD_FAILED_KEY = "error.document.s3.upload.failed";
    public static final String S3_DOWNLOAD_FAILED_KEY = "error.document.s3.download.failed";
    public static final String S3_DELETE_FAILED_KEY = "error.document.s3.delete.failed";
    public static final String S3_URL_GENERATION_FAILED_KEY = "error.document.s3.url.generation.failed";
    public static final String S3_EXPIRATION_INVALID_KEY = "error.document.s3.expiration.invalid";
    
    public S3OperationException(String message) {
        super(message);
    }
    
    public S3OperationException(String operation, MessageSource messageSource) {
        this(messageSource.getMessage(operation, new Object[]{""}, locale));
    }
    
    public S3OperationException(String operation, String error, MessageSource messageSource) {
        this(messageSource.getMessage(operation, new Object[]{error}, locale));
    }
}

