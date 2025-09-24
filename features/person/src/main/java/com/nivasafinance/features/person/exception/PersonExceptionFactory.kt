package com.nivasafinance.features.person.exception

import org.springframework.context.MessageSource
import java.util.UUID

object PersonExceptionFactory {

    fun notFound(id: UUID, messageSource: MessageSource): PersonNotFoundException {
        return PersonNotFoundException(id, messageSource)
    }

    fun createFailed(messageSource: MessageSource): PersonOperationException {
        return PersonOperationException("error.person.operation.create", messageSource)
    }

    fun updateFailed(messageSource: MessageSource): PersonOperationException {
        return PersonOperationException("error.person.operation.update", messageSource)
    }

    fun retrieveEntityFailed(messageSource: MessageSource): PersonOperationException {
        return PersonOperationException("error.person.operation.retrieve", messageSource)
    }

    fun primaryMobileAlreadyExists(
        mobileNumber: String,
        messageSource: MessageSource
    ): PersonPrimaryMobileAlreadyExistsException {
        return PersonPrimaryMobileAlreadyExistsException(mobileNumber, messageSource)
    }
}
