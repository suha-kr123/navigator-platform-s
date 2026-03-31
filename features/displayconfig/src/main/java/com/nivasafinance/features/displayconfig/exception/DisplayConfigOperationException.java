package com.nivasafinance.features.displayconfig.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import org.springframework.context.MessageSource;

public class DisplayConfigOperationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public DisplayConfigOperationException(String operation, MessageSource messageSource) {
        super(ExceptionUtils.createLocalizedMessage(
                operation,
                new Object[]{},
                messageSource
        ));
    }
}
