package com.nivasafinance.features.master.pincode.exception

import exception.ExceptionUtils
import exception.ValidationException
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
