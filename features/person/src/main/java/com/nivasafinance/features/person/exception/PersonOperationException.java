package com.nivasafinance.features.person.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import org.springframework.context.MessageSource;

public class PersonOperationException extends RuntimeException {
    public PersonOperationException(String messageCode, MessageSource messageSource) {
        super(ExceptionUtils.createLocalizedMessage(messageCode, null, messageSource));
    }
}

