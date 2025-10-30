package com.nivasafinance.features.lead.exception

import com.nivasafinance.common.exception.ExceptionUtils
import com.nivasafinance.common.exception.ValidationException
import org.springframework.context.MessageSource
import java.math.BigDecimal
import java.util.UUID

open class LeadValidationException(
    messageKey: String,
    args: Array<Any>? = null,
    messageSource: MessageSource
) : ValidationException(
    ExceptionUtils.createLocalizedMessage(messageKey, args, messageSource)
)

