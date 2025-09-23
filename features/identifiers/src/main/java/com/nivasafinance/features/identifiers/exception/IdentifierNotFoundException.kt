package com.nivasafinance.features.identifiers.exception

import exception.ExceptionUtils
import exception.ResourceNotFoundException
import org.springframework.context.MessageSource
import java.util.*

class IdentifierNotFoundException(
    identifierId: UUID,
    messageSource: MessageSource
) : ResourceNotFoundException(
    ExceptionUtils.createLocalizedMessage(
        "error.identifier.not.found",
        arrayOf(identifierId.toString()),
        messageSource
    )
)
