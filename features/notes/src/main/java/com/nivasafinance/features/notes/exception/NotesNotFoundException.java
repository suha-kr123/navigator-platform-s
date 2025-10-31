package com.nivasafinance.features.notes.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import com.nivasafinance.common.exception.ResourceNotFoundException;
import org.springframework.context.MessageSource;
import java.util.UUID;

public class NotesNotFoundException extends ResourceNotFoundException {
    private static final long serialVersionUID = 1L;

    public NotesNotFoundException(UUID notesId, MessageSource messageSource) {
        super(ExceptionUtils.createLocalizedMessage(
                "error.notes.not.found",
                new Object[]{notesId.toString()},
                messageSource
        ));
    }
}

