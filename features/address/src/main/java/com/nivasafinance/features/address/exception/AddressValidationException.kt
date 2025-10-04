package com.nivasafinance.features.address.exception

import com.nivasafinance.common.exception.ExceptionUtils
import com.nivasafinance.common.exception.ValidationException
import org.springframework.context.MessageSource

open class AddressValidationException(
    messageKey: String,
    args: Array<Any>? = null,
    messageSource: MessageSource
) : ValidationException(
    ExceptionUtils.createLocalizedMessage(messageKey, args, messageSource)
)

class AddressPincodeValidationException(
    pincode: String?,
    messageSource: MessageSource
) : AddressValidationException(
    "error.address.pincode.invalid",
    arrayOf(pincode ?: "null"),
    messageSource
)
