package com.nivasafinance.features.leadpersons.exception

import org.springframework.context.MessageSource
import java.util.*

object LeadPersonsExceptionFactory {

    fun notFound(leadPersonId: UUID, messageSource: MessageSource): LeadPersonsNotFoundException {
        return LeadPersonsNotFoundException(leadPersonId, messageSource)
    }

    fun createFailed(messageSource: MessageSource): LeadPersonsOperationException {
        return LeadPersonsOperationException("error.leadperson.operation.create", messageSource)
    }

    fun updateFailed(messageSource: MessageSource): LeadPersonsOperationException {
        return LeadPersonsOperationException("error.leadperson.operation.update", messageSource)
    }

    fun deleteFailed(messageSource: MessageSource): LeadPersonsOperationException {
        return LeadPersonsOperationException("error.leadperson.operation.delete", messageSource)
    }

    fun retrieveEntityFailed(messageSource: MessageSource): LeadPersonsOperationException {
        return LeadPersonsOperationException("error.leadperson.operation.retrieve", messageSource)
    }

    fun alreadyExists(leadId: UUID, personId: UUID, messageSource: MessageSource): LeadPersonsOperationException {
        return LeadPersonsOperationException("error.leadperson.already.exists", messageSource)
    }
}
