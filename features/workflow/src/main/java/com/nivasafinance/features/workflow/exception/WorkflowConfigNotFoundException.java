package com.nivasafinance.features.workflow.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import com.nivasafinance.common.exception.ResourceNotFoundException;
import org.springframework.context.MessageSource;

import java.io.Serial;

public class WorkflowConfigNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    private WorkflowConfigNotFoundException(String message) {
        super(message);
    }

    public static WorkflowConfigNotFoundException workflowConfigNotFound(String workflowConfigKey, MessageSource messageSource) {
        return new WorkflowConfigNotFoundException(
            ExceptionUtils.createLocalizedMessage(
                "error.workflow.config.not.found",
                new Object[]{workflowConfigKey},
                messageSource
            )
        );
    }
}
