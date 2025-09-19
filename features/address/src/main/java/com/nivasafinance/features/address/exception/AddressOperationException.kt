package com.nivasafinance.features.address.exception

import exception.ExceptionUtils
import org.springframework.context.MessageSource

class AddressOperationException(
    messageKey: String,
    messageSource: MessageSource
) : RuntimeException(
    ExceptionUtils.createLocalizedMessage(messageKey, null, messageSource)
)
