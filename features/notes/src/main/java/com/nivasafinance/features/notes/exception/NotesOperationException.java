package com.nivasafinance.features.notes.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import org.springframework.context.MessageSource;

public class NotesOperationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public NotesOperationException(String operation, MessageSource messageSource) {
        super(ExceptionUtils.createLocalizedMessage(
                operation,
                new Object[]{},
                messageSource
        ));
    }
}

