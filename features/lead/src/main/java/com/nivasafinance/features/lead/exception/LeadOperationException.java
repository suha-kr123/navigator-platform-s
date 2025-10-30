package com.nivasafinance.features.lead.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import org.springframework.context.MessageSource;

public class LeadOperationException extends RuntimeException {

    public LeadOperationException(String messageKey, MessageSource messageSource) {
        super(ExceptionUtils.INSTANCE.createLocalizedMessage(messageKey, null, messageSource));
    }
}
