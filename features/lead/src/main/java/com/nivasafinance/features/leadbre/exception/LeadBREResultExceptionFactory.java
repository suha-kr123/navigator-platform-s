package com.nivasafinance.features.leadbre.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import org.springframework.context.MessageSource;

import java.util.UUID;

public final class LeadBREResultExceptionFactory {

    private LeadBREResultExceptionFactory() {
    }

    public static LeadBREResultOperationException saveFailed(MessageSource messageSource) {
        return new LeadBREResultOperationException("error.lead.bre.result.save.failed", null, messageSource);
    }

    public static LeadBREResultNotFoundException notFound(UUID identifier, MessageSource messageSource) {
        String message = ExceptionUtils.createLocalizedMessage(
                "error.lead.bre.result.not.found",
                new Object[]{identifier},
                messageSource
        );
        return new LeadBREResultNotFoundException(message);
    }

    public static LeadBREResultOperationException retrieveFailed(MessageSource messageSource) {
        return new LeadBREResultOperationException("error.lead.bre.result.retrieve.failed", null, messageSource);
    }
}
