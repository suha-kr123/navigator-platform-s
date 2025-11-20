package com.nivasafinance.features.advisor.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import org.springframework.context.MessageSource;

public class AdvisorOperationException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public AdvisorOperationException(String messageKey, MessageSource messageSource) {
        super(ExceptionUtils.createLocalizedMessage(messageKey, null, messageSource));
    }
}

