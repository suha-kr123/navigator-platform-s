package com.nivasafinance.features.sourcechannel.exception;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

public class SourcingChannelOperationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SourcingChannelOperationException(String messageKey, MessageSource messageSource) {
        super(messageSource.getMessage(
                messageKey,
                null,
                "Sourcing channel operation failed",
                LocaleContextHolder.getLocale()
        ));
    }
}

