package com.nivasafinance.features.document.exception;

import com.nivasafinance.common.exception.BadRequestException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.io.Serial;
import java.util.Locale;

public class ConfigurationException extends BadRequestException {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    private static final Locale locale = LocaleContextHolder.getLocale();
    public static final String PROVIDER_NOT_CONFIGURED_KEY = "error.document.provider.not.configured";
    
    public ConfigurationException(String message) {
        super(message);
    }
    
    public ConfigurationException(String providerType, MessageSource messageSource) {
        this(messageSource.getMessage(PROVIDER_NOT_CONFIGURED_KEY, new Object[]{providerType}, locale));
    }
}


