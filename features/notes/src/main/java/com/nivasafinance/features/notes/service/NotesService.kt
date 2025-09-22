package com.nivasafinance.features.notes.service

import base.model.PaginatedResponse
import base.model.PaginationRequest
import com.nivasafinance.features.notes.dto.NotesRequest
import com.nivasafinance.features.notes.dto.NotesResponse
import com.nivasafinance.features.notes.dto.NotesUpdateRequest
import java.util.UUID

interface NotesService {
    fun createNotesByEntity(entityType: String, entityId: UUID, notesRequest: NotesRequest): NotesResponse
    fun updateNotesByEntity(entityType: String, entityId: UUID, notesId: UUID, notesUpdateRequest: NotesUpdateRequest): NotesResponse
    fun deleteNotesByEntity(entityType: String, entityId: UUID, notesId: UUID)
    fun getNotesByEntity(entityType: String, entityId: UUID, paginationRequest: PaginationRequest): PaginatedResponse<NotesResponse>
}       