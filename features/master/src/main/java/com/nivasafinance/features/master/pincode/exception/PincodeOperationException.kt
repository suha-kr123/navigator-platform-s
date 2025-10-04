package com.nivasafinance.features.master.pincode.exception

import com.nivasafinance.common.exception.ExceptionUtils
import org.springframework.context.MessageSource

class PincodeOperationException(
    messageKey: String,
    messageSource: MessageSource
) : RuntimeException(
    ExceptionUtils.createLocalizedMessage(messageKey, null, messageSource)
)
