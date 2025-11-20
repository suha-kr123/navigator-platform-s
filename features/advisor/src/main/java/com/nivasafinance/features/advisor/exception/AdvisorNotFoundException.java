package com.nivasafinance.features.advisor.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import com.nivasafinance.common.exception.ResourceNotFoundException;
import org.springframework.context.MessageSource;

import java.util.UUID;

public class AdvisorNotFoundException extends ResourceNotFoundException {
    
    private static final long serialVersionUID = 1L;
    
    public AdvisorNotFoundException(UUID identifier, MessageSource messageSource) {
        super(ExceptionUtils.createLocalizedMessage(
                "error.advisor.id.not.found",
                new Object[]{identifier.toString()},
                messageSource
        ));
    }
}

