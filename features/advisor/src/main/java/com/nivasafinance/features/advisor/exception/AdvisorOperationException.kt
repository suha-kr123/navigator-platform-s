package com.nivasafinance.features.advisor.exception

import exception.ExceptionUtils
import org.springframework.context.MessageSource

class AdvisorOperationException(
    messageKey: String,
    messageSource: MessageSource
) : RuntimeException(
    ExceptionUtils.createLocalizedMessage(messageKey, null, messageSource)
)
