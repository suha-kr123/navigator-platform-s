package com.nivasafinance.features.offices.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import com.nivasafinance.common.exception.ResourceNotFoundException;
import org.springframework.context.MessageSource;

import java.io.Serial;
import java.util.UUID;

public class OfficeNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    public OfficeNotFoundException(UUID id, MessageSource messageSource) {
        super(ExceptionUtils.createLocalizedMessage(
                "error.office.id.not.found",
                new Object[]{id.toString()},
                messageSource
        ));
    }
}

