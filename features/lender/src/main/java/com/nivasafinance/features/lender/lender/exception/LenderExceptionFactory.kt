package com.nivasafinance.features.lender.lender.exception

import org.springframework.context.MessageSource
import java.util.UUID

object LenderExceptionFactory {

    fun createFailed(messageSource: MessageSource): LenderOperationException {
        return LenderOperationException("error.lender.operation.create", messageSource)
    }

    fun lenderNotFound(lenderId: UUID, messageSource: MessageSource): LenderNotFoundException {
        return LenderNotFoundException(lenderId, messageSource)
    }

    fun lenderNotFound(key: String, messageSource: MessageSource): LenderKeyNotFoundException {
        return LenderKeyNotFoundException(key, messageSource)
    }
}
