package com.nivasafinance.features.task.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import com.nivasafinance.common.exception.ResourceNotFoundException;
import org.springframework.context.MessageSource;

import java.io.Serial;

public class TaskConfigNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1234567890123456790L;

    public TaskConfigNotFoundException(String taskConfigKey, MessageSource messageSource) {
        super(ExceptionUtils.createLocalizedMessage(
                "error.task.config.not.found",
                new Object[]{taskConfigKey},
                messageSource
        ));
    }
}

