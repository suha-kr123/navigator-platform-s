package com.nivasafinance.features.person.exception

import com.nivasafinance.common.exception.ExceptionUtils
import com.nivasafinance.common.exception.ValidationException
import org.springframework.context.MessageSource

open class PersonValidationException(
    messageKey: String,
    args: Array<Any>? = null,
    messageSource: MessageSource
) : ValidationException(
    ExceptionUtils.createLocalizedMessage(messageKey, args, messageSource)
)

class PersonPrimaryMobileAlreadyExistsException(
    mobileNumber: String,
    messageSource: MessageSource
) : PersonValidationException(
    "error.person.primary.mobile.already.exists",
    arrayOf(mobileNumber),
    messageSource
)
