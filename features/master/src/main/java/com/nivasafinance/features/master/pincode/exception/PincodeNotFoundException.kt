package com.nivasafinance.features.master.pincode.exception

import exception.ResourceNotFoundException
import org.springframework.context.MessageSource

class PincodeNotFoundException(
    pincode: String,
    messageSource: MessageSource
) : ResourceNotFoundException(
    exception.ExceptionUtils.createLocalizedMessage(
        "error.pincode.not.found",
        arrayOf(pincode),
        messageSource
    )
)
