package com.nivasafinance.features.task.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import org.springframework.context.MessageSource;

import java.io.Serial;

public class TaskOperationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1234567890123456792L;

    private TaskOperationException(String message) {
        super(message);
    }

    // Factory methods for common operation errors

    public static TaskOperationException taskConfigInactive(String taskConfigKey, MessageSource messageSource) {
        return new TaskOperationException(
            ExceptionUtils.createLocalizedMessage("error.task.config.inactive", 
                new Object[]{taskConfigKey}, messageSource)
        );
    }

    public static TaskOperationException dueDateInPast(String taskConfigKey, MessageSource messageSource) {
        return new TaskOperationException(
            ExceptionUtils.createLocalizedMessage("error.task.due.date.in.past", 
                new Object[]{taskConfigKey}, messageSource)
        );
    }

    public static TaskOperationException alreadyCompleted(String taskIdentifier, MessageSource messageSource) {
        return new TaskOperationException(
            ExceptionUtils.createLocalizedMessage("error.task.already.completed", 
                new Object[]{taskIdentifier}, messageSource)
        );
    }

    public static TaskOperationException cannotReassignCompleted(String taskIdentifier, MessageSource messageSource) {
        return new TaskOperationException(
            ExceptionUtils.createLocalizedMessage("error.task.cannot.reassign.completed", 
                new Object[]{taskIdentifier}, messageSource)
        );
    }

    public static TaskOperationException cannotRescheduleCompleted(String taskIdentifier, MessageSource messageSource) {
        return new TaskOperationException(
            ExceptionUtils.createLocalizedMessage("error.task.cannot.reschedule.completed", 
                new Object[]{taskIdentifier}, messageSource)
        );
    }

    public static TaskOperationException cannotReassignToSameUserOrRole(String taskIdentifier, MessageSource messageSource) {
        return new TaskOperationException(
            ExceptionUtils.createLocalizedMessage("error.task.cannot.reassign.same", 
                new Object[]{taskIdentifier}, messageSource)
        );
    }

    public static TaskOperationException rescheduleNotAllowed(String taskConfigKey, MessageSource messageSource) {
        return new TaskOperationException(
            ExceptionUtils.createLocalizedMessage("error.task.reschedule.not.allowed", 
                new Object[]{taskConfigKey}, messageSource)
        );
    }
}

