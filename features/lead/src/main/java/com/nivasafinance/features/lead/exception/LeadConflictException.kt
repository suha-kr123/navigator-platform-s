package com.nivasafinance.features.lead.exception

import com.nivasafinance.common.exception.ConflictException
import com.nivasafinance.common.exception.ExceptionUtils
import org.springframework.context.MessageSource
import java.util.UUID

open class LeadConflictException(
    messageKey: String,
    args: Array<Any>? = null,
    messageSource: MessageSource
) : ConflictException(
    ExceptionUtils.createLocalizedMessage(messageKey, args, messageSource)
)

class LeadAlreadyExistsException(
    leadId: UUID,
    messageSource: MessageSource
) : LeadConflictException(
    "error.lead.already.exists",
    arrayOf(leadId.toString()),
    messageSource
)
