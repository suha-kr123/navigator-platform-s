package com.nivasafinance.features.notes.service

import com.nivasafinance.features.notes.dto.NotesRequest
import com.nivasafinance.features.notes.dto.NotesResponse
import com.nivasafinance.features.notes.dto.NotesUpdateRequest
import com.nivasafinance.features.notes.enum.EntityType
import java.util.UUID

interface NotesService {
    fun createNotes(notesRequest: NotesRequest): NotesResponse
    fun updateNotes(notesId: UUID, notesUpdateRequest: NotesUpdateRequest): NotesResponse
    fun deleteNotes(notesId: UUID)
    fun getNotesById(notesId: UUID): NotesResponse
    fun getNotesByEntityTypeAndEntityId(entityType: EntityType, entityId: UUID): List<NotesResponse>
}