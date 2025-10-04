package com.nivasafinance.features.lead.exception

import com.nivasafinance.common.exception.ExceptionUtils
import com.nivasafinance.common.exception.ResourceNotFoundException
import org.springframework.context.MessageSource
import java.util.UUID

class LeadNotFoundException(
    leadId: UUID,
    messageSource: MessageSource
) : ResourceNotFoundException(
    ExceptionUtils.createLocalizedMessage(
        "error.lead.not.found",
        arrayOf(leadId.toString()),
        messageSource
    )
)
