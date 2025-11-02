package com.nivasafinance.features.person.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import com.nivasafinance.common.exception.ResourceNotFoundException;
import org.springframework.context.MessageSource;

import java.util.UUID;

public class PersonNotFoundException extends ResourceNotFoundException {
    
    public PersonNotFoundException(Long personId, MessageSource messageSource) {
        super(ExceptionUtils.createLocalizedMessage(
                "error.person.id.not.found",
                new Object[]{personId.toString()},
                messageSource
        ));
    }
}

