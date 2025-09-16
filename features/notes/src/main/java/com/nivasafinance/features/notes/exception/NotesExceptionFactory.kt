package com.nivasafinance.features.notes.exception

import com.nivasafinance.features.notes.enum.EntityType
import org.springframework.context.MessageSource
import java.util.*

object NotesExceptionFactory {

    fun notFound(notesId: UUID, messageSource: MessageSource): NotesNotFoundException {
        return NotesNotFoundException(notesId, messageSource)
    }

    fun createFailed(messageSource: MessageSource): NotesOperationException {
        return NotesOperationException("error.notes.operation.create", messageSource)
    }

    fun updateFailed(messageSource: MessageSource): NotesOperationException {
        return NotesOperationException("error.notes.operation.update", messageSource)
    }

    fun deleteFailed(messageSource: MessageSource): NotesOperationException {
        return NotesOperationException("error.notes.operation.delete", messageSource)
    }

    fun retrieveEntityFailed(messageSource: MessageSource): NotesOperationException {
        return NotesOperationException("error.notes.operation.retrieve", messageSource)
    }

    fun notesContentInvalid(notes: String?, messageSource: MessageSource): NotesValidationException {
        return NotesValidationException("Notes content cannot be null or empty", messageSource)
    }

    fun entityTypeInvalid(entityType: EntityType?, messageSource: MessageSource): NotesValidationException {
        return NotesValidationException("Entity type cannot be null", messageSource)
    }

    fun entityIdInvalid(entityId: UUID?, messageSource: MessageSource): NotesValidationException {
        return NotesValidationException("Entity ID cannot be null", messageSource)
    }

    fun parentIdInvalid(parentId: UUID?, messageSource: MessageSource): NotesValidationException {
        return NotesValidationException("Parent ID is invalid", messageSource)
    }
}
