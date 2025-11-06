package com.nivasafinance.features.master.pincode.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import com.nivasafinance.common.exception.ValidationException;
import org.springframework.context.MessageSource;

import java.io.Serial;

public class PincodeValidationException extends ValidationException {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    public PincodeValidationException(String pincode, MessageSource messageSource) {
        super(ExceptionUtils.createLocalizedMessage(
                "error.pincode.invalid",
                new Object[]{pincode != null ? pincode : "null"},
                messageSource
        ));
    }
}

