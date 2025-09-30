package com.nivasafinance.features.leadlender.exception

import exception.ExceptionUtils
import org.springframework.context.MessageSource
import java.util.UUID

object LeadLenderExceptionFactory {

    fun createFailed(messageSource: MessageSource): LeadLenderOperationException {
        return LeadLenderOperationException("error.lead.lender.operation.failed", messageSource)
    }

    fun leadLenderNotFound(id: UUID, messageSource: MessageSource): LeadLenderNotFoundException {
        val message = ExceptionUtils.createLocalizedMessage(
            "error.lead.lender.not.found",
            arrayOf(id.toString()),
            messageSource
        )
        return LeadLenderNotFoundException(message)
    }

    fun leadLenderNotFound(leadId: UUID, lenderKey: String, messageSource: MessageSource): LeadLenderNotFoundException {
        val message = ExceptionUtils.createLocalizedMessage(
            "error.lead.lender.by.lead.and.key.not.found",
            arrayOf(leadId.toString(), lenderKey),
            messageSource
        )
        return LeadLenderNotFoundException(message)
    }
}

class LeadLenderOperationException(
    messageKey: String,
    messageSource: MessageSource
) : RuntimeException(
    ExceptionUtils.createLocalizedMessage(messageKey, null, messageSource)
)
