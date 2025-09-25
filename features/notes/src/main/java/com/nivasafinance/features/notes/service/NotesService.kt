package com.nivasafinance.features.notes.service

import base.model.PaginatedResponse
import base.model.PaginationRequest
import com.nivasafinance.features.notes.dto.NotesRequest
import com.nivasafinance.features.notes.dto.NotesResponse
import com.nivasafinance.features.notes.dto.NotesUpdateRequest
import java.util.UUID

interface NotesService {
    fun createNotes(notesRequest: NotesRequest): NotesResponse
    fun updateNotes(notesId: UUID, notesUpdateRequest: NotesUpdateRequest): NotesResponse
    fun patchNotes(notesId: UUID, notesUpdateRequest: NotesUpdateRequest): NotesResponse
    fun deleteNotes(notesId: UUID)
    fun getNotesById(notesId: UUID): NotesResponse
    fun getAllNotes(paginationRequest: PaginationRequest): PaginatedResponse<NotesResponse>
}       