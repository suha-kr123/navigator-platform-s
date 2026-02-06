package com.nivasafinance.features.creditbureau.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import org.springframework.context.MessageSource;

import java.io.Serial;

public class CreditBureauOperationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -19094762348956213L;

    public CreditBureauOperationException(String messageKey, Object[] args, MessageSource messageSource) {
        super(ExceptionUtils.createLocalizedMessage(messageKey, args, messageSource));
    }

    public CreditBureauOperationException(String messageKey, Object[] args, MessageSource messageSource, Throwable cause) {
        super(ExceptionUtils.createLocalizedMessage(messageKey, args, messageSource), cause);
    }
}
