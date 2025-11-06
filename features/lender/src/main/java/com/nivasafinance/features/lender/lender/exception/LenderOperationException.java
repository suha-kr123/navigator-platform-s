package com.nivasafinance.features.lender.lender.exception;

import org.springframework.context.MessageSource;

import java.io.Serial;

public class LenderOperationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public LenderOperationException(String messageKey, MessageSource messageSource) {
        super(com.nivasafinance.common.exception.ExceptionUtils.createLocalizedMessage(messageKey, null, messageSource));
    }
}

