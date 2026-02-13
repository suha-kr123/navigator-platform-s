package com.nivasafinance.features.staff.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import org.springframework.context.MessageSource;

import java.io.Serial;

public class StaffOperationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public StaffOperationException(String messageKey, MessageSource messageSource) {
        super(ExceptionUtils.createLocalizedMessage(messageKey, null, messageSource));
    }
}
