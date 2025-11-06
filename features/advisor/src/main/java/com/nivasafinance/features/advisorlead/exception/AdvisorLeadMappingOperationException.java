package com.nivasafinance.features.advisorlead.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import org.springframework.context.MessageSource;

import java.io.Serial;

public class AdvisorLeadMappingOperationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public AdvisorLeadMappingOperationException(String messageKey, MessageSource messageSource) {
        super(ExceptionUtils.createLocalizedMessage(messageKey, null, messageSource));
    }
}

