package com.nivasafinance.features.advisorlead.exception

import exception.ExceptionUtils
import org.springframework.context.MessageSource

class AdvisorLeadMappingOperationException(
    messageKey: String,
    messageSource: MessageSource
) : RuntimeException(
    ExceptionUtils.createLocalizedMessage(messageKey, null, messageSource)
)
