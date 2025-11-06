package com.nivasafinance.features.task.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import com.nivasafinance.common.exception.ValidationException;
import org.springframework.context.MessageSource;

import java.io.Serial;

public class TaskValidationException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 1234567890123456791L;

    private TaskValidationException(String message) {
        super(message);
    }

    public static TaskValidationException requestRequired(MessageSource messageSource) {
        return new TaskValidationException(
            ExceptionUtils.createLocalizedMessage("error.task.request.required", null, messageSource)
        );
    }

    public static TaskValidationException taskIdRequired(MessageSource messageSource) {
        return new TaskValidationException(
            ExceptionUtils.createLocalizedMessage("error.task.id.required", null, messageSource)
        );
    }

    public static TaskValidationException taskConfigKeyRequired(MessageSource messageSource) {
        return new TaskValidationException(
            ExceptionUtils.createLocalizedMessage("error.task.config.key.required", null, messageSource)
        );
    }

    public static TaskValidationException assignmentRequired(MessageSource messageSource) {
        return new TaskValidationException(
            ExceptionUtils.createLocalizedMessage("error.task.assignment.required", null, messageSource)
        );
    }

    public static TaskValidationException outcomeRequired(MessageSource messageSource) {
        return new TaskValidationException(
            ExceptionUtils.createLocalizedMessage("error.task.outcome.required", null, messageSource)
        );
    }

    public static TaskValidationException newAssignmentRequired(MessageSource messageSource) {
        return new TaskValidationException(
            ExceptionUtils.createLocalizedMessage("error.task.new.assignment.required", null, messageSource)
        );
    }

    public static TaskValidationException dueDateRequired(MessageSource messageSource) {
        return new TaskValidationException(
            ExceptionUtils.createLocalizedMessage("error.task.due.date.required", null, messageSource)
        );
    }

    public static TaskValidationException invalidOutcome(String outcome, String taskConfigKey, MessageSource messageSource) {
        return new TaskValidationException(
            ExceptionUtils.createLocalizedMessage("error.task.outcome.invalid", 
                new Object[]{outcome, taskConfigKey}, messageSource)
        );
    }
}

