package com.nivasafinance.features.consent.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import com.nivasafinance.features.consent.enums.ConsentStatus;
import org.springframework.context.MessageSource;

import java.util.UUID;

public final class ConsentExceptionFactory {

    private ConsentExceptionFactory() {
    }

    public static ConsentOperationException saveFailed(MessageSource messageSource) {
        return new ConsentOperationException("error.consent.save.failed", null, messageSource);
    }

    public static ConsentOperationException retrieveByIdFailed(Long id, MessageSource messageSource) {
        return new ConsentOperationException("error.consent.retrieve.by.id.failed", new Object[]{id}, messageSource);
    }

    public static ConsentNotFoundException notFoundById(Long id, MessageSource messageSource) {
        String message = ExceptionUtils.createLocalizedMessage(
                "error.consent.not.found.by.id",
                new Object[]{id},
                messageSource
        );
        return new ConsentNotFoundException(message);
    }

    public static ConsentNotFoundException notFoundByIdentifier(UUID identifier, MessageSource messageSource) {
        String message = ExceptionUtils.createLocalizedMessage(
                "error.consent.not.found.by.identifier",
                new Object[]{identifier},
                messageSource
        );
        return new ConsentNotFoundException(message);
    }

    public static ConsentOperationException retrieveByIdentifierFailed(UUID identifier, MessageSource messageSource) {
        return new ConsentOperationException("error.consent.retrieve.by.identifier.failed", new Object[]{identifier}, messageSource);
    }

    public static ConsentOperationException invalidStatusForAccept(ConsentStatus status, MessageSource messageSource) {
        return new ConsentOperationException("error.consent.invalid.status.for.accept", new Object[]{status}, messageSource);
    }

    public static ConsentOperationException invalidStatusForResend(ConsentStatus status, MessageSource messageSource) {
        return new ConsentOperationException("error.consent.invalid.status.for.resend", new Object[]{status}, messageSource);
    }

    public static ConsentOperationException invalidStatusForWithdrawn(ConsentStatus status, MessageSource messageSource) {
        return new ConsentOperationException("error.consent.invalid.status.for.withdrawn", new Object[]{status}, messageSource);
    }
}
