package com.nivasafinance.features.call.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import org.springframework.context.MessageSource;

import java.util.List;
import java.util.UUID;

public final class CallLogExceptionFactory {

    private CallLogExceptionFactory() {
        // Utility class
    }

    public static CallLogOperationException saveFailed(MessageSource messageSource) {
        return new CallLogOperationException("error.call.log.save.failed", null, messageSource);
    }

    public static CallLogOperationException retrieveByIdFailed(Long id, MessageSource messageSource) {
        return new CallLogOperationException("error.call.log.retrieve.by.id.failed", new Object[]{id}, messageSource);
    }

    public static CallLogOperationException retrieveByIdentifierFailed(UUID identifier, MessageSource messageSource) {
        return new CallLogOperationException("error.call.log.retrieve.by.identifier.failed", new Object[]{identifier}, messageSource);
    }

    public static CallLogOperationException retrieveByProviderIdFailed(String providerId, MessageSource messageSource) {
        return new CallLogOperationException("error.call.log.retrieve.by.provider.id.failed", new Object[]{providerId}, messageSource);
    }

    public static CallLogOperationException retrieveByIdsFailed(List<Long> ids, MessageSource messageSource) {
        return new CallLogOperationException(
                "error.call.log.retrieve.by.ids.failed",
                new Object[]{ids == null ? null : ids.toString()},
                messageSource
        );
    }

    public static CallLogNotFoundException notFoundById(Long id, MessageSource messageSource) {
        String message = ExceptionUtils.createLocalizedMessage(
                "error.call.log.not.found.by.id",
                new Object[]{id},
                messageSource
        );
        return new CallLogNotFoundException(message);
    }

    public static CallLogNotFoundException notFoundByIdentifier(UUID identifier, MessageSource messageSource) {
        String message = ExceptionUtils.createLocalizedMessage(
                "error.call.log.not.found.by.identifier",
                new Object[]{identifier},
                messageSource
        );
        return new CallLogNotFoundException(message);
    }

    public static CallLogNotFoundException notFoundByProviderId(String providerId, MessageSource messageSource) {
        String message = ExceptionUtils.createLocalizedMessage(
                "error.call.log.not.found.by.provider.id",
                new Object[]{providerId},
                messageSource
        );
        return new CallLogNotFoundException(message);
    }
}


