package com.nivasafinance.features.income.exception

import org.springframework.context.MessageSource
import java.util.*

object IncomeDetailsExceptionFactory {

    fun notFound(incomeDetailsId: UUID, messageSource: MessageSource): IncomeDetailsNotFoundException {
        return IncomeDetailsNotFoundException(incomeDetailsId, messageSource)
    }

    fun createFailed(messageSource: MessageSource): IncomeDetailsOperationException {
        return IncomeDetailsOperationException("error.income.operation.create", messageSource)
    }

    fun deleteFailed(messageSource: MessageSource): IncomeDetailsOperationException {
        return IncomeDetailsOperationException("error.income.operation.delete", messageSource)
    }

    fun retrieveEntityFailed(messageSource: MessageSource): IncomeDetailsOperationException {
        return IncomeDetailsOperationException("error.income.operation.retrieve", messageSource)
    }

    fun unsupportedEntityType(entityType: String, messageSource: MessageSource): IncomeDetailsOperationException {
        return IncomeDetailsOperationException("error.income.unsupported.entity.type", messageSource)
    }
}
