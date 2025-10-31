package com.nivasafinance.features.lead.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import org.springframework.context.MessageSource;

import java.io.Serial;

public class LeadOperationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 4827612938475629347L;

    public LeadOperationException(String messageKey, MessageSource messageSource) {
        super(ExceptionUtils.createLocalizedMessage(messageKey, null, messageSource));
    }
}
