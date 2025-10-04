package com.nivasafinance.features.lead.exception

import com.nivasafinance.common.exception.ExceptionUtils
import org.springframework.context.MessageSource

class LeadOperationException(
    messageKey: String,
    messageSource: MessageSource
) : RuntimeException(
    ExceptionUtils.createLocalizedMessage(messageKey, null, messageSource)
)
