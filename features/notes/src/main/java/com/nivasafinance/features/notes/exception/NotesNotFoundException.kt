package com.nivasafinance.features.notes.exception

import com.nivasafinance.common.exception.ExceptionUtils
import com.nivasafinance.common.exception.ResourceNotFoundException
import org.springframework.context.MessageSource
import java.util.UUID

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
