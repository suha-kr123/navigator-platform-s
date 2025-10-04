package com.nivasafinance.features.advisor.exception

import com.nivasafinance.common.exception.ExceptionUtils
import org.springframework.context.MessageSource

class AdvisorOperationException(
    messageKey: String,
    messageSource: MessageSource
) : RuntimeException(
    ExceptionUtils.createLocalizedMessage(messageKey, null, messageSource)
)
