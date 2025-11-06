package com.nivasafinance.features.master.codemaster.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import org.springframework.context.MessageSource;

import java.io.Serial;

public class CodeMasterOperationException extends RuntimeException {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    public CodeMasterOperationException(String messageKey, MessageSource messageSource) {
        super(ExceptionUtils.createLocalizedMessage(messageKey, null, messageSource));
    }
}

