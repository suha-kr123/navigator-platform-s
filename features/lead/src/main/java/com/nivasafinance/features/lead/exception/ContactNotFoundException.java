package com.nivasafinance.features.lead.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import com.nivasafinance.common.exception.ResourceNotFoundException;
import org.springframework.context.MessageSource;

import java.io.Serial;
import java.util.UUID;

public class ContactNotFoundException extends ResourceNotFoundException {
    @Serial
    private static final long serialVersionUID = 5619238475619283747L;

    public ContactNotFoundException(Long contactId, MessageSource messageSource) {
        super(ExceptionUtils.createLocalizedMessage(
                "error.contact.not.found",
                new Object[]{contactId.toString()},
                messageSource
        ));
    }

    public ContactNotFoundException(UUID contactIdentifier, MessageSource messageSource) {
        super(ExceptionUtils.createLocalizedMessage(
                "error.contact.not.found",
                new Object[]{contactIdentifier.toString()},
                messageSource
        ));
    }
}