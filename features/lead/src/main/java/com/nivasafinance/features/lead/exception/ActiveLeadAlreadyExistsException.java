package com.nivasafinance.features.lead.exception;

import org.springframework.context.MessageSource;

import java.io.Serial;

public class ActiveLeadAlreadyExistsException extends LeadConflictException {

    @Serial
    private static final long serialVersionUID = 3847561923847561923L;

    public ActiveLeadAlreadyExistsException(String phoneNo, MessageSource messageSource) {
        super("error.lead.active.already.exists.with.phone.no", new Object[]{phoneNo}, messageSource);
    }
}
