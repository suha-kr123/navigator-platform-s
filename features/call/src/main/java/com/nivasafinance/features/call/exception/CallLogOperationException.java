package com.nivasafinance.features.call.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import org.springframework.context.MessageSource;

import java.io.Serial;

public class CallLogOperationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -19094762348956213L;

    public CallLogOperationException(String messageKey, Object[] args, MessageSource messageSource) {
        super(ExceptionUtils.createLocalizedMessage(messageKey, args, messageSource));
    }
}


