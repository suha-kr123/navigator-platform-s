package com.nivasafinance.features.lead.exception;

import com.nivasafinance.common.exception.ConflictException;
import com.nivasafinance.common.exception.ExceptionUtils;
import org.springframework.context.MessageSource;

public class LeadConflictException extends ConflictException {

    public LeadConflictException(String messageKey, Object[] args, MessageSource messageSource) {
        super(ExceptionUtils.INSTANCE.createLocalizedMessage(messageKey, args, messageSource));
    }
}
