package com.nivasafinance.features.notes.repository

import com.nivasafinance.features.notes.entity.Notes
import com.nivasafinance.features.notes.enum.EntityType
import com.nivasafinance.features.notes.exception.NotesExceptionFactory
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.util.*

@Service
class NotesRepositoryWrapper(
    private val notesRepository: NotesRepository,
    private val messageSource: MessageSource
) {

    fun saveWithException(notes: Notes): Notes {
        return try {
            notesRepository.save(notes)
        } catch (e: RuntimeException) {
            throw NotesExceptionFactory.createFailed(messageSource)
        }
    }

    fun deleteByIdWithException(notesId: UUID) {
        try {
            notesRepository.deleteById(notesId)
        } catch (e: RuntimeException) {
            throw NotesExceptionFactory.deleteFailed(messageSource)
        }
    }

    fun findByIdWithException(notesId: UUID): Notes {
        return try {
            notesRepository.findById(notesId).orElseThrow {
                NotesExceptionFactory.notFound(notesId, messageSource)
            }
        } catch (e: Exception) {
            throw NotesExceptionFactory.notFound(notesId, messageSource)
        }
    }

    fun findAllByEntityTypeAndEntityId(entityType: EntityType, entityId: UUID): List<Notes> {
        return notesRepository.findAllByEntityTypeAndEntityId(entityType, entityId)
    }

    fun findAllByParentIdWithException(parentId: UUID): List<Notes> {
        return try {
            notesRepository.findAllByParentId(parentId)
        } catch (e: Exception) {
            throw NotesExceptionFactory.retrieveEntityFailed(messageSource)
        }
    }
}