package com.nivasafinance.features.notes.exception

import exception.ExceptionUtils
import exception.ResourceNotFoundException
import org.springframework.context.MessageSource
import java.util.*

class NotesNotFoundException(
    notesId: UUID,
    messageSource: MessageSource
) : ResourceNotFoundException(
    ExceptionUtils.createLocalizedMessage(
        "error.notes.not.found",
        arrayOf(notesId.toString()),
        messageSource
    )
)
