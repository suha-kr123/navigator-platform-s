package com.nivasafinance.features.master.pincode.exception

import com.nivasafinance.common.exception.ExceptionUtils
import com.nivasafinance.common.exception.ResourceNotFoundException
import org.springframework.context.MessageSource

class PincodeNotFoundException(
    pincode: String,
    messageSource: MessageSource
) : ResourceNotFoundException(
    ExceptionUtils.createLocalizedMessage(
        "error.pincode.not.found",
        arrayOf(pincode),
        messageSource
    )
)
