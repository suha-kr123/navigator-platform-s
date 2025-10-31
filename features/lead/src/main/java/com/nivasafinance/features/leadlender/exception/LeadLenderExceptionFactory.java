package com.nivasafinance.features.leadlender.exception;

import com.nivasafinance.common.exception.ExceptionUtils;
import org.springframework.context.MessageSource;

import java.io.Serial;
import java.util.UUID;

public class LeadLenderExceptionFactory {

    private LeadLenderExceptionFactory() {
        // Private constructor to prevent instantiation
    }

    public static LeadLenderOperationException createFailed(MessageSource messageSource) {
        return new LeadLenderOperationException("error.lead.lender.operation.failed", messageSource);
    }

    public static LeadLenderNotFoundException leadLenderNotFound(MessageSource messageSource) {
        String message = ExceptionUtils.createLocalizedMessage(
            "error.lead.lender.not.found",
            null,
            messageSource
        );
        return new LeadLenderNotFoundException(message);
    }

    public static LeadLenderNotFoundException leadLenderNotFound(UUID lenderIdentifier, 
                                                                  MessageSource messageSource) {
        String message = ExceptionUtils.createLocalizedMessage(
            "error.lead.lender.not.found.by.identifier",
            new Object[]{lenderIdentifier.toString()},
            messageSource
        );
        return new LeadLenderNotFoundException(message);
    }

    public static LeadLenderNotFoundException leadLenderNotFound(UUID leadId, String lenderKey, 
                                                                  MessageSource messageSource) {
        String message = ExceptionUtils.createLocalizedMessage(
            "error.lead.lender.by.lead.and.key.not.found",
            new Object[]{leadId.toString(), lenderKey},
            messageSource
        );
        return new LeadLenderNotFoundException(message);
    }

    public static class LeadLenderOperationException extends RuntimeException {
        
        @Serial
        private static final long serialVersionUID = 1L;
        
        public LeadLenderOperationException(String messageKey, MessageSource messageSource) {
            super(ExceptionUtils.createLocalizedMessage(messageKey, null, messageSource));
        }
    }
}
