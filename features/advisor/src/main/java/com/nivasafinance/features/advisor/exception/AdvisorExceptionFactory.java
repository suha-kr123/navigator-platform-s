package com.nivasafinance.features.advisor.exception;

import org.springframework.context.MessageSource;

import java.util.UUID;

public class AdvisorExceptionFactory {

    public static AdvisorNotFoundException notFound(UUID id, MessageSource messageSource) {
        return new AdvisorNotFoundException(id, messageSource);
    }

    public static AdvisorOperationException createFailed(MessageSource messageSource) {
        return new AdvisorOperationException("error.advisor.operation.create", messageSource);
    }

    public static AdvisorOperationException updateFailed(MessageSource messageSource) {
        return new AdvisorOperationException("error.advisor.operation.update", messageSource);
    }

    public static AdvisorOperationException deleteFailed(MessageSource messageSource) {
        return new AdvisorOperationException("error.advisor.operation.delete", messageSource);
    }

    public static AdvisorOperationException retrieveEntityFailed(MessageSource messageSource) {
        return new AdvisorOperationException("error.advisor.operation.retrieve", messageSource);
    }
}

