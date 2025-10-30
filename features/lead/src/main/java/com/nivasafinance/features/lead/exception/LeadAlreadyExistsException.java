package com.nivasafinance.features.lead.exception;

import org.springframework.context.MessageSource;

import java.util.UUID;

public class LeadAlreadyExistsException extends LeadConflictException {

    public LeadAlreadyExistsException(UUID leadId, MessageSource messageSource) {
        super("error.lead.already.exists", new Object[]{leadId.toString()}, messageSource);
    }
}
