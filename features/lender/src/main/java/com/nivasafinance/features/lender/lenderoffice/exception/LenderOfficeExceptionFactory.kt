package com.nivasafinance.features.lender.lenderoffice.exception

import exception.ExceptionUtils
import org.springframework.context.MessageSource
import java.util.UUID

object LenderOfficeExceptionFactory {

    fun createFailed(messageSource: MessageSource): LenderOfficeOperationException {
        return LenderOfficeOperationException("error.lender.office.operation.failed", messageSource)
    }

    fun lenderOfficeNotFound(id: UUID, messageSource: MessageSource): LenderOfficeNotFoundException {
        val message = ExceptionUtils.createLocalizedMessage(
            "error.lender.office.not.found",
            arrayOf(id.toString()),
            messageSource
        )
        return LenderOfficeNotFoundException(message)
    }

    fun lenderOfficeKeyNotFound(key: String, messageSource: MessageSource): LenderOfficeNotFoundException {
        val message = ExceptionUtils.createLocalizedMessage(
            "error.lender.office.key.not.found",
            arrayOf(key),
            messageSource
        )
        return LenderOfficeNotFoundException(message)
    }
}

class LenderOfficeOperationException(
    messageKey: String,
    messageSource: MessageSource
) : RuntimeException(
    ExceptionUtils.createLocalizedMessage(messageKey, null, messageSource)
)
