package com.nivasafinance.features.notes.service

import com.nivasafinance.features.notes.dto.NotesRequest
import com.nivasafinance.features.notes.dto.NotesResponse
import com.nivasafinance.features.notes.dto.NotesUpdateRequest
import com.nivasafinance.features.notes.entity.Notes
import com.nivasafinance.features.notes.enum.EntityType
import com.nivasafinance.features.notes.repository.NotesRepositoryWrapper
import org.springframework.stereotype.Service
import java.util.*

@Service
class NotesServiceImpl(
    private val notesRepositoryWrapper: NotesRepositoryWrapper
) : NotesService {

    override fun createNotes(notesRequest: NotesRequest): NotesResponse {
        val notes = Notes(
            notes = notesRequest.notes,
            entityType = notesRequest.entityType,
            entityId = notesRequest.entityId
        )
        val savedNotes = notesRepositoryWrapper.saveWithException(notes)
        return toNotesResponse(savedNotes)
    }

    override fun updateNotes(notesId: UUID, notesUpdateRequest: NotesUpdateRequest): NotesResponse {
        val existingNotes = notesRepositoryWrapper.findByIdWithException(notesId)
        val updatedNotes = existingNotes.copy(
            notes = notesUpdateRequest.notes ?: existingNotes.notes
        )
        val savedNotes = notesRepositoryWrapper.saveWithException(updatedNotes)
        return toNotesResponse(savedNotes)
    }
    
    override fun deleteNotes(notesId: UUID) {
        notesRepositoryWrapper.deleteByIdWithException(notesId)
    }

    override fun getNotesById(notesId: UUID): NotesResponse {
        val notes = notesRepositoryWrapper.findByIdWithException(notesId)
        return toNotesResponse(notes)
    }
    
    override fun getNotesByEntityTypeAndEntityId(entityType: EntityType, entityId: UUID): List<NotesResponse> {
        val notesList = notesRepositoryWrapper.findAllByEntityTypeAndEntityId(entityType, entityId)
        return notesList.map { toNotesResponse(it) }
    }

    private fun toNotesResponse(notes: Notes): NotesResponse {
        return NotesResponse(
            id = notes.id!!,
            notes = notes.notes,
            entityType = notes.entityType,
            entityId = notes.entityId
        )
    }
}