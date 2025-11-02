package com.nivasafinance.features.person.exception;

import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.common.exception.ExceptionUtils;
import org.springframework.context.MessageSource;

import java.io.Serial;

public class PersonPrimaryMobileAlreadyExistsException extends BadRequestException {

    @Serial
    private static final long serialVersionUID = 9934434L;

    public PersonPrimaryMobileAlreadyExistsException(String mobileNumber, MessageSource messageSource) {
        super(ExceptionUtils.createLocalizedMessage(
                "error.person.mobile.primary.already.exists",
                new Object[]{mobileNumber},
                messageSource
        ));
    }
}

