package com.nivasafinance.features.sourcechannel.exception;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

public class SourcingChannelNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SourcingChannelNotFoundException(Long id, MessageSource messageSource) {
        super(messageSource.getMessage(
                "error.sourcingchannel.notfound.id",
                new Object[]{id},
                "Sourcing channel not found with id: " + id,
                LocaleContextHolder.getLocale()
        ));
    }

    public SourcingChannelNotFoundException(String sourcingIdentifier, MessageSource messageSource) {
        super(messageSource.getMessage(
                "error.sourcingchannel.notfound.identifier",
                new Object[]{sourcingIdentifier},
                "Sourcing channel not found with identifier: " + sourcingIdentifier,
                LocaleContextHolder.getLocale()
        ));
    }
}

