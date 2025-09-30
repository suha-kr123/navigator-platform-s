package com.nivasafinance.features.advisorleadmapping.exception

import exception.ExceptionUtils
import org.springframework.context.MessageSource

class AdvisorLeadMappingOperationException(
    messageKey: String,
    messageSource: MessageSource
) : RuntimeException(
    ExceptionUtils.createLocalizedMessage(messageKey, null, messageSource)
)
