package com.nivasafinance.features.person.exception

import com.nivasafinance.common.exception.ExceptionUtils
import org.springframework.context.MessageSource

class PersonOperationException(
    messageKey: String,
    messageSource: MessageSource
) : RuntimeException(
    ExceptionUtils.createLocalizedMessage(messageKey, null, messageSource)
)
