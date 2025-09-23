package com.nivasafinance.features.stagedefinitions.exception

import exception.ExceptionUtils
import org.springframework.context.MessageSource

class StageDefinitionOperationException(
    operation: String,
    messageSource: MessageSource
) : RuntimeException(
    ExceptionUtils.createLocalizedMessage(
        operation,
        emptyArray(),
        messageSource
    )
)
