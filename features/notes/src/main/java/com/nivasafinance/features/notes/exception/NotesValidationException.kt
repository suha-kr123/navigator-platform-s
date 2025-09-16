package com.nivasafinance.features.notes.exception

import exception.ExceptionUtils
import exception.ValidationException
import org.springframework.context.MessageSource

class NotesValidationException(
    reason: String,
    messageSource: MessageSource
) : ValidationException(
    ExceptionUtils.createLocalizedMessage(
        "error.notes.validation",
        arrayOf(reason),
        messageSource
    )
)
