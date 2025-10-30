package com.nivasafinance.features.lead.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import com.nivasafinance.common.exception.ResourceNotFoundException;
import org.springframework.context.MessageSource;

import java.util.UUID;

public class LeadNotFoundException extends ResourceNotFoundException {

    public LeadNotFoundException(UUID leadId, MessageSource messageSource) {
        super(ExceptionUtils.INSTANCE.createLocalizedMessage(
                "error.lead.not.found",
                new Object[]{leadId.toString()},
                messageSource
        ));
    }
}
