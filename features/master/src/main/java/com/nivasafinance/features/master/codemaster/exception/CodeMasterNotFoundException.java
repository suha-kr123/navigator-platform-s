package com.nivasafinance.features.master.codemaster.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import com.nivasafinance.common.exception.ResourceNotFoundException;
import org.springframework.context.MessageSource;

import java.io.Serial;

public class CodeMasterNotFoundException extends ResourceNotFoundException {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    public CodeMasterNotFoundException(String codeName, MessageSource messageSource) {
        super(ExceptionUtils.createLocalizedMessage(
                "error.codemaster.code.name.not.found",
                new Object[]{codeName},
                messageSource
        ));
    }
}

