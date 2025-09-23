package com.nivasafinance.features.leadpersons.exception

import exception.ExceptionUtils
import org.springframework.context.MessageSource

class LeadPersonsOperationException(
    messageKey: String,
    messageSource: MessageSource
) : RuntimeException(
    ExceptionUtils.createLocalizedMessage(messageKey, null, messageSource)
)
