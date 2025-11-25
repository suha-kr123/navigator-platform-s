package com.nivasafinance.features.workflow.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import com.nivasafinance.common.exception.ValidationException;
import org.springframework.context.MessageSource;

import java.io.Serial;

public class WorkflowSelectionException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    private WorkflowSelectionException(String message) {
        super(message);
    }

    public static WorkflowSelectionException cannotDetermineWorkflow(String eventName, MessageSource messageSource) {
        return new WorkflowSelectionException(
                ExceptionUtils.createLocalizedMessage(
                        "error.workflow.selection.cannot.determine",
                        new Object[]{eventName},
                        messageSource
                )
        );
    }

    public static WorkflowSelectionException eventNameNotFound(MessageSource messageSource) {
        return new WorkflowSelectionException(
                ExceptionUtils.createLocalizedMessage(
                        "error.workflow.selection.event.name.not.found",
                        new Object[]{},
                        messageSource
                )
        );
    }
}

