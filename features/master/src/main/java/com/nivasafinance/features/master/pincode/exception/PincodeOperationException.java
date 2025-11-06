package com.nivasafinance.features.master.pincode.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import org.springframework.context.MessageSource;

import java.io.Serial;

public class PincodeOperationException extends RuntimeException {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    public PincodeOperationException(String messageKey, MessageSource messageSource) {
        super(ExceptionUtils.createLocalizedMessage(messageKey, null, messageSource));
    }
}

