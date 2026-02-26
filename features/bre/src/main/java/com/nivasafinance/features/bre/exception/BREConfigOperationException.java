package com.nivasafinance.features.bre.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import org.springframework.context.MessageSource;

import java.io.Serial;

public class BREConfigOperationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -19094762348956216L;

    public BREConfigOperationException(String messageKey, Object[] args, MessageSource messageSource) {
        super(ExceptionUtils.createLocalizedMessage(messageKey, args, messageSource));
    }
}
