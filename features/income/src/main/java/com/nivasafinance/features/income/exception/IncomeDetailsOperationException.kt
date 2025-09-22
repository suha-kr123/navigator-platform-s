package com.nivasafinance.features.income.exception

import exception.ExceptionUtils
import org.springframework.context.MessageSource

class IncomeDetailsOperationException(
    operation: String,
    messageSource: MessageSource
) : RuntimeException(
    ExceptionUtils.createLocalizedMessage(
        operation,
        emptyArray(),
        messageSource
    )
)
