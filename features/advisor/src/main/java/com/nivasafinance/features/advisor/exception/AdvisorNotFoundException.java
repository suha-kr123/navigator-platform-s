package com.nivasafinance.features.advisor.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import com.nivasafinance.common.exception.ResourceNotFoundException;
import org.springframework.context.MessageSource;

import java.io.Serial;
import java.util.UUID;

public class AdvisorNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    public AdvisorNotFoundException(UUID advisorId, MessageSource messageSource) {
        super(ExceptionUtils.createLocalizedMessage(
                "error.advisor.id.not.found",
                new Object[]{advisorId.toString()},
                messageSource
        ));
    }
}

