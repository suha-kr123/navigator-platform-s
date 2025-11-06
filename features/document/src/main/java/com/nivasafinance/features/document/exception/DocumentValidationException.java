package com.nivasafinance.features.document.exception;

import com.nivasafinance.common.exception.ValidationException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.io.Serial;
import java.util.Locale;

public class DocumentValidationException extends ValidationException {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    private static final Locale locale = LocaleContextHolder.getLocale();
    public static final String DOCUMENT_VALIDATION_KEY = "error.document.validation";
    
    public DocumentValidationException(String message) {
        super(message);
    }
    
    public DocumentValidationException(String message, MessageSource messageSource) {
        this(messageSource.getMessage(DOCUMENT_VALIDATION_KEY, new Object[]{message}, locale));
    }
}


