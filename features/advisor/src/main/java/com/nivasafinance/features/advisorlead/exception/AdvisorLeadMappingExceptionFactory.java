package com.nivasafinance.features.advisorlead.exception;

import org.springframework.context.MessageSource;

import java.util.UUID;

public class AdvisorLeadMappingExceptionFactory {

    public static AdvisorLeadMappingNotFoundException notFound(UUID id, MessageSource messageSource) {
        return new AdvisorLeadMappingNotFoundException(id, messageSource);
    }

    public static AdvisorLeadMappingOperationException createFailed(MessageSource messageSource) {
        return new AdvisorLeadMappingOperationException("error.advisor.lead.mapping.operation.create", messageSource);
    }

    public static AdvisorLeadMappingOperationException updateFailed(MessageSource messageSource) {
        return new AdvisorLeadMappingOperationException("error.advisor.lead.mapping.operation.update", messageSource);
    }

    public static AdvisorLeadMappingOperationException deleteFailed(MessageSource messageSource) {
        return new AdvisorLeadMappingOperationException("error.advisor.lead.mapping.operation.delete", messageSource);
    }

    public static AdvisorLeadMappingOperationException retrieveEntityFailed(MessageSource messageSource) {
        return new AdvisorLeadMappingOperationException("error.advisor.lead.mapping.operation.retrieve", messageSource);
    }
}

