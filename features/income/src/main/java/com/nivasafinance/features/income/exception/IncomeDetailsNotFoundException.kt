package com.nivasafinance.features.income.exception

import exception.ExceptionUtils
import exception.ResourceNotFoundException
import org.springframework.context.MessageSource
import java.util.*

class IncomeDetailsNotFoundException(
    incomeDetailsId: UUID,
    messageSource: MessageSource
) : ResourceNotFoundException(
    ExceptionUtils.createLocalizedMessage(
        "error.income.not.found",
        arrayOf(incomeDetailsId.toString()),
        messageSource
    )
)
