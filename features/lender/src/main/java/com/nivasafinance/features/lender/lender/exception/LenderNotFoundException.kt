package com.nivasafinance.features.lender.lender.exception

import exception.ExceptionUtils
import exception.ResourceNotFoundException
import org.springframework.context.MessageSource
import java.util.UUID

class LenderNotFoundException(
    lenderId: UUID,
    messageSource: MessageSource
) : ResourceNotFoundException(
    ExceptionUtils.createLocalizedMessage(
        "error.lender.not.found",
        arrayOf(lenderId.toString()),
        messageSource
    )
)

class LenderKeyNotFoundException(
    key: String,
    messageSource: MessageSource
) : ResourceNotFoundException(
    ExceptionUtils.createLocalizedMessage(
        "error.lender.key.not.found",
        arrayOf(key),
        messageSource
    )
)
