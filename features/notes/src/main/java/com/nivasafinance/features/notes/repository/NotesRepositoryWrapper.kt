package com.nivasafinance.features.notes.repository

import com.nivasafinance.features.notes.entity.Notes
import com.nivasafinance.features.notes.exception.NotesExceptionFactory
import org.springframework.context.MessageSource
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
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

    fun findAllByEntityTypeAndEntityId(entityType: String, entityId: UUID, pageable: Pageable): Page<Notes> {
        return try {
            notesRepository.findAllByEntityTypeAndEntityId(entityType, entityId, pageable)
        } catch (e: Exception) {
            throw NotesExceptionFactory.retrieveEntityFailed(messageSource)
        }
    }

    fun findAllByEntityTypeAndEntityId(entityType: String, entityId: UUID): List<Notes> {
        return try {
            notesRepository.findAllByEntityTypeAndEntityId(entityType, entityId, Pageable.unpaged()).content
        } catch (e: Exception) {
            throw NotesExceptionFactory.retrieveEntityFailed(messageSource)
        }
    }
}