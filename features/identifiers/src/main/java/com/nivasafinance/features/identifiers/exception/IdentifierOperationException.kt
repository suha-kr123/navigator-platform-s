package com.nivasafinance.features.identifiers.exception

import com.nivasafinance.common.exception.ExceptionUtils
import org.springframework.context.MessageSource

class IdentifierOperationException(
    operation: String,
    messageSource: MessageSource
) : RuntimeException(
    ExceptionUtils.createLocalizedMessage(
        operation,
        emptyArray(),
        messageSource
    )
)
