package com.nivasafinance.features.workflow.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import com.nivasafinance.common.exception.ValidationException;
import org.springframework.context.MessageSource;

import java.io.Serial;

public class WorkflowConfigValidationException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    private WorkflowConfigValidationException(String message) {
        super(message);
    }

    public static WorkflowConfigValidationException invalidWorkflowConfiguration(MessageSource messageSource) {
        return new WorkflowConfigValidationException(
            ExceptionUtils.createLocalizedMessage(
                "error.workflow.config.invalid",
                new Object[]{},
                messageSource
            )
        );
    }

    public static WorkflowConfigValidationException landingStageNotFound(String workflowConfigKey, MessageSource messageSource) {
        return new WorkflowConfigValidationException(
            ExceptionUtils.createLocalizedMessage(
                "error.workflow.config.landing.stage.not.found",
                new Object[]{workflowConfigKey},
                messageSource
            )
        );
    }
    
    public static WorkflowConfigValidationException stageNotFoundInWorkflow(String stageKey, String workflowConfigKey, MessageSource messageSource) {
        return new WorkflowConfigValidationException(
            ExceptionUtils.createLocalizedMessage(
                "error.workflow.config.stage.not.found",
                new Object[]{stageKey, workflowConfigKey},
                messageSource
            )
        );
    }
    
    public static WorkflowConfigValidationException adhocTaskNotAllowedForStage(String stageKey, String taskConfigKey, MessageSource messageSource) {
        return new WorkflowConfigValidationException(
            ExceptionUtils.createLocalizedMessage(
                "error.workflow.config.adhoc.task.not.allowed",
                new Object[]{taskConfigKey, stageKey},
                messageSource
            )
        );
    }
}
