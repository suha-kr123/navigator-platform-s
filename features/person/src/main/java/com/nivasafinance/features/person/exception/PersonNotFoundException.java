package com.nivasafinance.features.person.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import com.nivasafinance.common.exception.ResourceNotFoundException;
import org.springframework.context.MessageSource;

import java.io.Serial;

public class PersonNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 932223434L;
    
    public PersonNotFoundException(Long personId, MessageSource messageSource) {
        super(ExceptionUtils.createLocalizedMessage(
                "error.person.id.not.found",
                new Object[]{personId.toString()},
                messageSource
        ));
    }
}

