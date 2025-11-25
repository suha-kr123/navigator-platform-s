package com.nivasafinance.features.workflow.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import com.nivasafinance.common.exception.ValidationException;
import org.springframework.context.MessageSource;

import java.io.Serial;

public class WorkflowConfigParseException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    private WorkflowConfigParseException(String message) {
        super(message);
    }

    public static WorkflowConfigParseException failedToParse(MessageSource messageSource) {
        return new WorkflowConfigParseException(
            ExceptionUtils.createLocalizedMessage(
                "error.workflow.config.parse.failed",
                new Object[]{},
                messageSource
            )
        );
    }
}

