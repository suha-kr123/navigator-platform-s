package com.nivasafinance.features.offices.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import org.springframework.context.MessageSource;

import java.io.Serial;

public class OfficeOperationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public OfficeOperationException(String messageKey, MessageSource messageSource) {
        super(ExceptionUtils.createLocalizedMessage(messageKey, null, messageSource));
    }
}
