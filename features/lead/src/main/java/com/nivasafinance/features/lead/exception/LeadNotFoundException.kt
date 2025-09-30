package com.nivasafinance.features.lead.exception

import exception.ResourceNotFoundException
import org.springframework.context.MessageSource
import java.util.*

class LeadNotFoundException(
    leadId: UUID,
    messageSource: MessageSource
) : ResourceNotFoundException(
    exception.ExceptionUtils.createLocalizedMessage(
        "error.lead.not.found",
        arrayOf(leadId.toString()),
        messageSource
    )
)
