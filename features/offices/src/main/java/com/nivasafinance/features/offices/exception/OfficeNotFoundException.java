package com.nivasafinance.features.offices.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import com.nivasafinance.common.exception.ResourceNotFoundException;
import org.springframework.context.MessageSource;

import java.io.Serial;

public class OfficeNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    public OfficeNotFoundException(String key, MessageSource messageSource) {
        super(ExceptionUtils.createLocalizedMessage(
                "error.office.key.not.found",
                new Object[]{key},
                messageSource
        ));
    }
}

