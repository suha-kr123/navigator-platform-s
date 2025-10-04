package com.nivasafinance.features.master.pincode.exception

import com.nivasafinance.common.exception.ExceptionUtils
import com.nivasafinance.common.exception.ValidationException
import org.springframework.context.MessageSource

class PincodeValidationException(
    pincode: String?,
    messageSource: MessageSource
) : ValidationException(
    ExceptionUtils.createLocalizedMessage(
        "error.pincode.invalid",
        arrayOf(pincode ?: "null"),
        messageSource
    )
)
