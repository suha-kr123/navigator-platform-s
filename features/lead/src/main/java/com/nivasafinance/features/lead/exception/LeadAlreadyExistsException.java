package com.nivasafinance.features.lead.exception;

import org.springframework.context.MessageSource;

import java.io.Serial;
import java.util.UUID;

public class LeadAlreadyExistsException extends LeadConflictException {

    @Serial
    private static final long serialVersionUID = 1923847561923847561L;

    public LeadAlreadyExistsException(UUID leadId, MessageSource messageSource) {
        super("error.lead.already.exists", new Object[]{leadId.toString()}, messageSource);
    }
}
