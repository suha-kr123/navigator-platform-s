package com.nivasafinance.features.identifiers.exception

import org.springframework.context.MessageSource
import java.util.*

object IdentifierExceptionFactory {

    fun notFound(identifierId: UUID, messageSource: MessageSource): IdentifierNotFoundException {
        return IdentifierNotFoundException(identifierId, messageSource)
    }

    fun createFailed(messageSource: MessageSource): IdentifierOperationException {
        return IdentifierOperationException("error.identifier.operation.create", messageSource)
    }

    fun deleteFailed(messageSource: MessageSource): IdentifierOperationException {
        return IdentifierOperationException("error.identifier.operation.delete", messageSource)
    }

    fun retrieveEntityFailed(messageSource: MessageSource): IdentifierOperationException {
        return IdentifierOperationException("error.identifier.operation.retrieve", messageSource)
    }

}
