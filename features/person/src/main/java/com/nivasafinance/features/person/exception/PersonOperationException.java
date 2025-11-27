package com.nivasafinance.features.person.exception;

import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.common.exception.ExceptionUtils;
import org.springframework.context.MessageSource;

import java.io.Serial;

public class PersonOperationException extends BadRequestException {
    @Serial
    private static final long serialVersionUID = 882423L;
    public PersonOperationException(String messageCode, MessageSource messageSource) {
        super(ExceptionUtils.createLocalizedMessage(messageCode, null, messageSource));
    }
}

