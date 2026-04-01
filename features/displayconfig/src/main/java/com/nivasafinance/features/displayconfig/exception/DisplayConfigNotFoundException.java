package com.nivasafinance.features.displayconfig.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import com.nivasafinance.common.exception.ResourceNotFoundException;
import org.springframework.context.MessageSource;

public class DisplayConfigNotFoundException extends ResourceNotFoundException {
    private static final long serialVersionUID = 1L;

    public DisplayConfigNotFoundException(String appType, MessageSource messageSource) {
        super(ExceptionUtils.createLocalizedMessage(
                "error.displayconfig.not.found",
                new Object[]{appType},
                messageSource
        ));
    }
}
