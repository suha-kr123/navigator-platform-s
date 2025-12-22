package com.nivasafinance.features.lead.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import com.nivasafinance.common.exception.ResourceNotFoundException;
import org.springframework.context.MessageSource;

import java.io.Serial;
import java.util.UUID;

public class LeadNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 5619238475619283746L;

    public LeadNotFoundException(Long LeadId, MessageSource messageSource) {
        super(ExceptionUtils.createLocalizedMessage(
                "error.lead.not.found",
                new Object[]{LeadId.toString()},
                messageSource
        ));
    }

    public LeadNotFoundException(UUID leadId, MessageSource messageSource) {
        super(ExceptionUtils.createLocalizedMessage(
                "error.lead.not.found",
                new Object[]{leadId.toString()},
                messageSource
        ));
    }
}
