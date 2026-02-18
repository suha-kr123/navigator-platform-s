package com.nivasafinance.features.creditbureau.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import org.springframework.context.MessageSource;

import java.util.UUID;

public final class CreditBureauExceptionFactory {

    private CreditBureauExceptionFactory() {
        // Utility class
    }

    public static CreditBureauOperationException saveFailed(MessageSource messageSource) {
        return new CreditBureauOperationException("error.creditbureau.enquiry.save.failed", null, messageSource);
    }

    public static CreditBureauOperationException retrieveByIdFailed(Long id, MessageSource messageSource) {
        return new CreditBureauOperationException("error.creditbureau.enquiry.retrieve.by.id.failed", new Object[]{id}, messageSource);
    }

    public static CreditBureauOperationException retrieveByIdentifierFailed(UUID identifier, MessageSource messageSource) {
        return new CreditBureauOperationException("error.creditbureau.enquiry.retrieve.by.identifier.failed", new Object[]{identifier}, messageSource);
    }

    public static CreditBureauOperationException executeFlowFailed(Long enquiryId, Throwable cause, MessageSource messageSource) {
        return new CreditBureauOperationException("error.creditbureau.enquiry.execute.flow.failed", new Object[]{enquiryId}, messageSource, cause);
    }

    public static CreditBureauNotFoundException notFoundById(Long id, MessageSource messageSource) {
        String message = ExceptionUtils.createLocalizedMessage(
                "error.creditbureau.enquiry.not.found.by.id",
                new Object[]{id},
                messageSource
        );
        return new CreditBureauNotFoundException(message);
    }

    public static CreditBureauNotFoundException notFoundByIdentifier(UUID identifier, MessageSource messageSource) {
        String message = ExceptionUtils.createLocalizedMessage(
                "error.creditbureau.enquiry.not.found.by.identifier",
                new Object[]{identifier},
                messageSource
        );
        return new CreditBureauNotFoundException(message);
    }

    public static CreditBureauOperationException enquiryConsentNotLinkable(MessageSource messageSource) {
        return new CreditBureauOperationException("error.creditbureau.enquiry.consent.not.linkable", null, messageSource);
    }
}
