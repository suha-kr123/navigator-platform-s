package com.nivasafinance.features.tasks.exception

import exception.ExceptionUtils
import org.springframework.context.MessageSource

class TaskOperationException(
    messageKey: String,
    messageSource: MessageSource
) : RuntimeException(
    ExceptionUtils.createLocalizedMessage(messageKey, null, messageSource)
)
