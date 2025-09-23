package com.nivasafinance.features.advisor.exception

import org.springframework.context.MessageSource
import java.util.UUID

object AdvisorExceptionFactory {

    fun notFound(id: UUID, messageSource: MessageSource): AdvisorNotFoundException {
        return AdvisorNotFoundException(id, messageSource)
    }

    fun createFailed(messageSource: MessageSource): AdvisorOperationException {
        return AdvisorOperationException("error.advisor.operation.create", messageSource)
    }

    fun updateFailed(messageSource: MessageSource): AdvisorOperationException {
        return AdvisorOperationException("error.advisor.operation.update", messageSource)
    }

    fun deleteFailed(messageSource: MessageSource): AdvisorOperationException {
        return AdvisorOperationException("error.advisor.operation.delete", messageSource)
    }

    fun retrieveEntityFailed(messageSource: MessageSource): AdvisorOperationException {
        return AdvisorOperationException("error.advisor.operation.retrieve", messageSource)
    }
}
