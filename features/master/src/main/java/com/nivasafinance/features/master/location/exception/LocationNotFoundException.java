package com.nivasafinance.features.master.location.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import com.nivasafinance.common.exception.ResourceNotFoundException;
import org.springframework.context.MessageSource;

import java.io.Serial;

public class LocationNotFoundException extends ResourceNotFoundException {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    public LocationNotFoundException(String entityType, Long id, MessageSource messageSource) {
        super(ExceptionUtils.createLocalizedMessage(
                "error.location.not.found",
                new Object[]{entityType, id},
                messageSource
        ));
    }
    
    public LocationNotFoundException(String message) {
        super(message);
    }
}

