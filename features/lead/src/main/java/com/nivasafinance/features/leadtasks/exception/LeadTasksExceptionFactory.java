package com.nivasafinance.features.leadtasks.exception;

import java.util.UUID;

import org.springframework.context.MessageSource;

import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.common.exception.ExceptionUtils;

public class LeadTasksExceptionFactory {

    private LeadTasksExceptionFactory() {
        // Private constructor to prevent instantiation
    }

    public static BadRequestException stageNotFound(MessageSource messageSource) {
        return new BadRequestException(ExceptionUtils.createLocalizedMessage("error.lead.stage.not.found", null, messageSource));
    }

    public static BadRequestException leadTaskNotFound(Long taskId, Long leadId, MessageSource messageSource) {
        return new BadRequestException(ExceptionUtils.createLocalizedMessage("error.lead.task.not.found", new Object[]{taskId, leadId}, messageSource));
    }

    public static BadRequestException invalidTaskDetails(Long taskId, UUID leadId, MessageSource messageSource) {
        return new BadRequestException(ExceptionUtils.createLocalizedMessage("error.lead.task.details.invalid", new Object[]{taskId, leadId}, messageSource));
    }

    public static BadRequestException taskNotFound(UUID taskIdentifier, MessageSource messageSource) {
        return new BadRequestException(ExceptionUtils.createLocalizedMessage("error.task.not.found", new Object[]{taskIdentifier}, messageSource));
    }
}
