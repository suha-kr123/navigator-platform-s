package com.nivasafinance.features.lead.exception;

import com.nivasafinance.common.exception.ConflictException;
import com.nivasafinance.common.exception.ExceptionUtils;
import org.springframework.context.MessageSource;

import java.io.Serial;

public class LeadConflictException extends ConflictException {

    @Serial
    private static final long serialVersionUID = 8475619238475612938L;

    public LeadConflictException(String messageKey, Object[] args, MessageSource messageSource) {
        super(ExceptionUtils.INSTANCE.createLocalizedMessage(messageKey, args, messageSource));
    }
}
