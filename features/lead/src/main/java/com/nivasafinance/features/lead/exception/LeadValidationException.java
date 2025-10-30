package com.nivasafinance.features.lead.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import com.nivasafinance.common.exception.ValidationException;
import org.springframework.context.MessageSource;

import java.io.Serial;

public class LeadValidationException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 2938475619283746502L;

    public LeadValidationException(String messageKey, Object[] args, MessageSource messageSource) {
        super(ExceptionUtils.INSTANCE.createLocalizedMessage(messageKey, args, messageSource));
    }
}
