package com.nivasafinance.features.leadqueues.exception;

import org.springframework.context.MessageSource;

import com.nivasafinance.common.exception.ExceptionUtils;
import com.nivasafinance.common.exception.ResourceNotFoundException;

import java.io.Serial;

public class QueueConfigNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1234567890123456789L;

    public QueueConfigNotFoundException(String message) {
        super(message);
    }

    public static QueueConfigNotFoundException notFoundByName(String name, MessageSource messageSource) {
        String message = ExceptionUtils.createLocalizedMessage(
                "error.queue.config.not.found.by.name",
                new Object[] { name },
                messageSource);
        return new QueueConfigNotFoundException(message);
    }
}
