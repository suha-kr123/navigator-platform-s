package com.nivasafinance.features.lender.lender.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import com.nivasafinance.common.exception.ResourceNotFoundException;
import org.springframework.context.MessageSource;

import java.io.Serial;
import java.util.UUID;

public class LenderNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    public LenderNotFoundException(UUID lenderId, MessageSource messageSource) {
        super(ExceptionUtils.createLocalizedMessage(
                "error.lender.not.found",
                new Object[]{lenderId.toString()},
                messageSource
        ));
    }
}

