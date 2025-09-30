package com.nivasafinance.features.advisorleadmapping.exception

import org.springframework.context.MessageSource
import java.util.UUID

object AdvisorLeadMappingExceptionFactory {

    fun notFound(id: UUID, messageSource: MessageSource): AdvisorLeadMappingNotFoundException {
        return AdvisorLeadMappingNotFoundException(id, messageSource)
    }

    fun createFailed(messageSource: MessageSource): AdvisorLeadMappingOperationException {
        return AdvisorLeadMappingOperationException("error.advisor.lead.mapping.operation.create", messageSource)
    }

    fun updateFailed(messageSource: MessageSource): AdvisorLeadMappingOperationException {
        return AdvisorLeadMappingOperationException("error.advisor.lead.mapping.operation.update", messageSource)
    }

    fun deleteFailed(messageSource: MessageSource): AdvisorLeadMappingOperationException {
        return AdvisorLeadMappingOperationException("error.advisor.lead.mapping.operation.delete", messageSource)
    }

    fun retrieveEntityFailed(messageSource: MessageSource): AdvisorLeadMappingOperationException {
        return AdvisorLeadMappingOperationException("error.advisor.lead.mapping.operation.retrieve", messageSource)
    }
}
