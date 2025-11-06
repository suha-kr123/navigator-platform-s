package com.nivasafinance.features.lender.lender.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import com.nivasafinance.common.exception.ResourceNotFoundException;
import org.springframework.context.MessageSource;

import java.io.Serial;

public class LenderKeyNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    public LenderKeyNotFoundException(String key, MessageSource messageSource) {
        super(ExceptionUtils.createLocalizedMessage(
                "error.lender.key.not.found",
                new Object[]{key},
                messageSource
        ));
    }
}

