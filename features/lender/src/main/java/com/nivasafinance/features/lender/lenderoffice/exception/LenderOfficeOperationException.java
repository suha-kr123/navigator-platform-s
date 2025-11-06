package com.nivasafinance.features.lender.lenderoffice.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import org.springframework.context.MessageSource;

import java.io.Serial;

public class LenderOfficeOperationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public LenderOfficeOperationException(String messageKey, MessageSource messageSource) {
        super(ExceptionUtils.createLocalizedMessage(messageKey, null, messageSource));
    }
}

