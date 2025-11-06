package com.nivasafinance.features.task.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import com.nivasafinance.common.exception.ResourceNotFoundException;
import org.springframework.context.MessageSource;

import java.io.Serial;
import java.util.UUID;

public class TaskNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1234567890123456789L;

    private TaskNotFoundException(String message) {
        super(message);
    }

    public static TaskNotFoundException taskNotFound(String taskIdentifier, MessageSource messageSource) {
        return new TaskNotFoundException(
            ExceptionUtils.createLocalizedMessage(
                "error.task.not.found",
                new Object[]{taskIdentifier},
                messageSource
            )
        );
    }

    public static TaskNotFoundException taskNotFoundByUuid(UUID taskUuid, MessageSource messageSource) {
        return new TaskNotFoundException(
            ExceptionUtils.createLocalizedMessage(
                "error.task.not.found",
                new Object[]{taskUuid},
                messageSource
            )
        );
    }
}

