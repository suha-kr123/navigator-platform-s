package com.nivasafinance.features.taskdefinitions.exception

import exception.ExceptionUtils
import org.springframework.context.MessageSource

class TaskDefinitionOperationException(
    operation: String,
    messageSource: MessageSource
) : RuntimeException(
    ExceptionUtils.createLocalizedMessage(
        operation,
        emptyArray(),
        messageSource
    )
)
