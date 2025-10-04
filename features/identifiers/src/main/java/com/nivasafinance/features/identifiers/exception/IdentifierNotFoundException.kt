package com.nivasafinance.features.identifiers.exception

import com.nivasafinance.common.exception.ExceptionUtils
import com.nivasafinance.common.exception.ResourceNotFoundException
import org.springframework.context.MessageSource
import java.util.UUID

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
