package com.nivasafinance.features.lender.lender.exception

import exception.ExceptionUtils
import org.springframework.context.MessageSource

class LenderOperationException(
    messageKey: String,
    messageSource: MessageSource
) : RuntimeException(
    ExceptionUtils.createLocalizedMessage(messageKey, null, messageSource)
)
