package com.nivasafinance.features.person.exception;

import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.common.exception.ExceptionUtils;
import org.springframework.context.MessageSource;

import java.io.Serial;

public class PersonMobileNumberNotFoundException extends BadRequestException {

    @Serial
    private static final long serialVersionUID = 9934232434L;
    public PersonMobileNumberNotFoundException(String mobileNo, MessageSource messageSource) {
        super(ExceptionUtils.createLocalizedMessage(
                "error.person.mobile.not.found",
                new Object[]{mobileNo},
                messageSource
        ));
    }
}

