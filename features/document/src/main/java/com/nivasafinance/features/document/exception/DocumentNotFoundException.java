package com.nivasafinance.features.document.exception;

import com.nivasafinance.common.exception.ResourceNotFoundException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.io.Serial;
import java.util.Locale;
import java.util.UUID;

public class DocumentNotFoundException extends ResourceNotFoundException {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    private static final Locale locale = LocaleContextHolder.getLocale();
    public static final String DOCUMENT_NOT_FOUND_KEY = "error.document.not.found";
    
    public DocumentNotFoundException(String message) {
        super(message);
    }
    
    public DocumentNotFoundException(UUID documentId, MessageSource messageSource) {
        this(messageSource.getMessage(DOCUMENT_NOT_FOUND_KEY, new Object[]{documentId}, locale));
    }
}


