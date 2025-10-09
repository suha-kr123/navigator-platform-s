package com.nivasafinance.features.notes.exception

import org.springframework.context.MessageSource
import java.util.*

object NotesExceptionFactory {

    fun notFound(notesId: UUID, messageSource: MessageSource): NotesNotFoundException {
        return NotesNotFoundException(notesId, messageSource)
    }

    fun createFailed(messageSource: MessageSource): NotesOperationException {
        return NotesOperationException("error.notes.operation.create", messageSource)
    }

    fun deleteFailed(messageSource: MessageSource): NotesOperationException {
        return NotesOperationException("error.notes.operation.delete", messageSource)
    }

    fun retrieveEntityFailed(messageSource: MessageSource): NotesOperationException {
        return NotesOperationException("error.notes.operation.retrieve", messageSource)
    }
}
