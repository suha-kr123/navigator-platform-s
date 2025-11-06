package com.nivasafinance.features.master.codemaster.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import com.nivasafinance.common.exception.ResourceNotFoundException;
import org.springframework.context.MessageSource;

import java.io.Serial;

public class CodeValueKeyNotFoundException extends ResourceNotFoundException {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    public CodeValueKeyNotFoundException(String key, MessageSource messageSource) {
        super(ExceptionUtils.createLocalizedMessage(
                "error.codevalue.key.not.found",
                new Object[]{key},
                messageSource
        ));
    }
}

