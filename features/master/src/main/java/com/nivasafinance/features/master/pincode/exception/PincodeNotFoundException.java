package com.nivasafinance.features.master.pincode.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import com.nivasafinance.common.exception.ResourceNotFoundException;
import org.springframework.context.MessageSource;

import java.io.Serial;

public class PincodeNotFoundException extends ResourceNotFoundException {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    public PincodeNotFoundException(String pincode, MessageSource messageSource) {
        super(ExceptionUtils.createLocalizedMessage(
                "error.pincode.not.found",
                new Object[]{pincode},
                messageSource
        ));
    }
}

