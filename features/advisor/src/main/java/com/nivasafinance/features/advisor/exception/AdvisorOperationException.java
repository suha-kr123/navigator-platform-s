package com.nivasafinance.features.advisor.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import org.springframework.context.MessageSource;

import java.io.Serial;

public class AdvisorOperationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public AdvisorOperationException(String messageKey, MessageSource messageSource) {
        super(ExceptionUtils.createLocalizedMessage(messageKey, null, messageSource));
    }
}

