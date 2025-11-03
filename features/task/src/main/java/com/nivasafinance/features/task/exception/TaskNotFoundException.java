package com.nivasafinance.features.task.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import com.nivasafinance.common.exception.ResourceNotFoundException;
import org.springframework.context.MessageSource;

import java.io.Serial;

public class TaskNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1234567890123456789L;

    public TaskNotFoundException(Long taskId, MessageSource messageSource) {
        super(ExceptionUtils.createLocalizedMessage(
                "error.task.not.found",
                new Object[]{taskId},
                messageSource
        ));
    }
}

