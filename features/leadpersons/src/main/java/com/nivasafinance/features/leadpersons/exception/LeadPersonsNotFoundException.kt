package com.nivasafinance.features.leadpersons.exception

import exception.ResourceNotFoundException
import org.springframework.context.MessageSource
import java.util.*

class LeadPersonsNotFoundException(
    leadPersonId: UUID,
    messageSource: MessageSource
) : ResourceNotFoundException(
    exception.ExceptionUtils.createLocalizedMessage(
        "error.leadperson.not.found",
        arrayOf(leadPersonId.toString()),
        messageSource
    )
)
