package com.nivasafinance.features.leadbre.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import org.springframework.context.MessageSource;

import java.io.Serial;

public class LeadBREResultOperationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 3948276519384756293L;

    public LeadBREResultOperationException(String messageKey, Object[] args, MessageSource messageSource) {
        super(ExceptionUtils.createLocalizedMessage(messageKey, args, messageSource));
    }
}
