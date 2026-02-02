package com.nivasafinance.features.advisor.exception;

import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.common.exception.ExceptionUtils;
import org.springframework.context.MessageSource;

import java.util.UUID;

public final class AdvisorExceptionFactory {

    private AdvisorExceptionFactory() {
    }

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

    public static BadRequestException personAlreadyExists(Long personId, MessageSource messageSource) {
        return new BadRequestException(ExceptionUtils.createLocalizedMessage(
                "error.advisor.person.already.exists",
                new Object[]{personId},
                messageSource
        ));
    }

    public static AdvisorOperationException noCurrentUser(MessageSource messageSource) {
        return new AdvisorOperationException("error.advisor.operation.no.current.user", messageSource);
    }
}
