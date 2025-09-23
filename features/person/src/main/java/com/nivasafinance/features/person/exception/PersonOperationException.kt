package com.nivasafinance.features.person.exception

import exception.ExceptionUtils
import org.springframework.context.MessageSource

class PersonOperationException(
    messageKey: String,
    messageSource: MessageSource
) : RuntimeException(
    ExceptionUtils.createLocalizedMessage(messageKey, null, messageSource)
)
