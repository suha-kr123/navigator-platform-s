package com.nivasafinance.features.master.pincode.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import org.springframework.context.MessageSource;

public class PincodeExceptionFactory {
    
    private static final int PINCODE_LENGTH = 6;
    
    public PincodeExceptionFactory(MessageSource messageSource) {
    }
    
    public PincodeNotFoundException notFound(String pincode, MessageSource messageSource) {
        return new PincodeNotFoundException(pincode, messageSource);
    }
    
    public PincodeOperationException retrieveEntityFailed(MessageSource messageSource) {
        return new PincodeOperationException("error.pincode.operation.retrieve", messageSource);
    }
    
    public PincodeValidationException pincodeInvalid(String pincode, MessageSource messageSource) {
        return new PincodeValidationException(pincode, messageSource);
    }
    
    public void validatePincode(String pincode, MessageSource messageSource) {
        ExceptionUtils.requireNotBlank(pincode, "pincode", "error.pincode.invalid", messageSource);
        
        if (pincode == null || pincode.isBlank() || pincode.length() != PINCODE_LENGTH) {
            throw pincodeInvalid(pincode, messageSource);
        }
    }
}

