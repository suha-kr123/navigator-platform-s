package com.nivasafinance.features.consent.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import org.springframework.context.MessageSource;

import java.io.Serial;

public class ConsentOperationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ConsentOperationException(String messageKey, Object[] args, MessageSource messageSource) {
        super(ExceptionUtils.createLocalizedMessage(messageKey, args, messageSource));
    }

    public ConsentOperationException(String messageKey, Object[] args, MessageSource messageSource, Throwable cause) {
        super(ExceptionUtils.createLocalizedMessage(messageKey, args, messageSource), cause);
    }
}
