package com.nivasafinance.features.workflow.exception;

import com.nivasafinance.common.exception.ValidationException;

/**
 * Custom validation exception for workflow operations.
 * Extends ValidationException to maintain consistency with global exception handling.
 */
public class WorkflowValidationException extends ValidationException {

    private static final long serialVersionUID = 1L;

    public WorkflowValidationException(String message) {
        super(message);
    }

    public static WorkflowValidationException nullEntityId() {
        return new WorkflowValidationException("entityId cannot be null");
    }

    public static WorkflowValidationException nullEntityType() {
        return new WorkflowValidationException("entityType cannot be null");
    }

    public static WorkflowValidationException nullOrEmptyStageKey() {
        return new WorkflowValidationException("stageKey cannot be null or empty");
    }

    public static WorkflowValidationException nullOrEmptyWorkflowConfigKey() {
        return new WorkflowValidationException("workflowConfigKey cannot be null or empty");
    }

    public static WorkflowValidationException nullOrEmptyTaskConfigKey() {
        return new WorkflowValidationException("taskConfigKey cannot be null or empty");
    }

    public static WorkflowValidationException nullOrEmptyToStageKey() {
        return new WorkflowValidationException("toStageKey cannot be null or empty");
    }

    public static WorkflowValidationException actionAlreadyProcessed() {
        return new WorkflowValidationException("Pending action has already been processed");
    }

    public static WorkflowValidationException pendingActionNotFound() {
        return new WorkflowValidationException("Pending workflow action not found");
    }
}

