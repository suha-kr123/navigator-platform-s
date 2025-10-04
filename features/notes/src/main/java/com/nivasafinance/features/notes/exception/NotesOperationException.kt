package com.nivasafinance.features.notes.exception

import com.nivasafinance.common.exception.ExceptionUtils
import org.springframework.context.MessageSource

class NotesOperationException(
    operation: String,
    messageSource: MessageSource
) : RuntimeException(
    ExceptionUtils.createLocalizedMessage(
        operation,
        emptyArray(),
        messageSource
    )
)
