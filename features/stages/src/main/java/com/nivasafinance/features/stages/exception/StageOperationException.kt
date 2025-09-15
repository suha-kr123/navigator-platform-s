package com.nivasafinance.features.stages.exception

import exception.ExceptionUtils
import org.springframework.context.MessageSource

class StageOperationException(
    messageKey: String,
    messageSource: MessageSource
) : RuntimeException(
    ExceptionUtils.createLocalizedMessage(messageKey, null, messageSource)
)
