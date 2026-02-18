package com.nivasafinance.features.task.exception;

import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.common.exception.ExceptionUtils;
import com.nivasafinance.common.enums.EntityType;
import org.springframework.context.MessageSource;

import java.io.Serial;
import java.util.UUID;

public class TaskOperationException extends BadRequestException {

    @Serial
    private static final long serialVersionUID = 1234567890123456792L;

    private TaskOperationException(String message) {
        super(message);
    }

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

    public static TaskOperationException alreadyCompleted(UUID taskIdentifier, MessageSource messageSource) {
        return new TaskOperationException(
            ExceptionUtils.createLocalizedMessage("error.task.already.completed", 
                new Object[]{taskIdentifier}, messageSource)
        );
    }

    public static TaskOperationException cannotReassignCompleted(UUID taskIdentifier, MessageSource messageSource) {
        return new TaskOperationException(
            ExceptionUtils.createLocalizedMessage("error.task.cannot.reassign.completed", 
                new Object[]{taskIdentifier}, messageSource)
        );
    }

    public static TaskOperationException cannotRescheduleCompleted(UUID taskIdentifier, MessageSource messageSource) {
        return new TaskOperationException(
            ExceptionUtils.createLocalizedMessage("error.task.cannot.reschedule.completed", 
                new Object[]{taskIdentifier}, messageSource)
        );
    }

    public static TaskOperationException cannotReassignToSameUserOrRole(UUID taskIdentifier, MessageSource messageSource) {
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

    public static TaskOperationException entityServiceNotFound(EntityType entityType, MessageSource messageSource) {
        return new TaskOperationException(
            ExceptionUtils.createLocalizedMessage("error.task.entity.service.not.found", 
                new Object[]{entityType.name()}, messageSource)
        );
    }   

    public static TaskOperationException adhocTaskNotAllowed(String taskConfigKey, MessageSource messageSource) {
        return new TaskOperationException(
            ExceptionUtils.createLocalizedMessage("error.task.adhoc.task.not.allowed", 
                new Object[]{taskConfigKey}, messageSource)
        );
    }
}

