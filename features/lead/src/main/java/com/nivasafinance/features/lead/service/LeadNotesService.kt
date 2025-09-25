package com.nivasafinance.features.lead.service

import base.model.PaginatedResponse
import base.model.PaginationRequest
import com.nivasafinance.features.lead.dto.LeadNotesResponse
import com.nivasafinance.features.notes.dto.NotesRequest
import com.nivasafinance.features.notes.dto.NotesUpdateRequest
import java.util.UUID

interface LeadNotesService {
    fun addNotesToLead(leadId: UUID, taskId: UUID?, addNotesToLeadRequest: NotesRequest): LeadNotesResponse
    fun getTaskNotes(
        leadId: UUID,
        taskId: UUID?,
        paginationRequest: PaginationRequest
    ): PaginatedResponse<List<LeadNotesResponse>>
    fun getNotesById(leadId: UUID, taskId: UUID?, notesId: UUID): LeadNotesResponse
    fun updateNotesById(
        leadId: UUID,
        taskId: UUID?,
        notesId: UUID,
        updateNotesRequest: NotesUpdateRequest
    ): LeadNotesResponse
    fun patchNotesById(
        leadId: UUID,
        taskId: UUID?,
        notesId: UUID,
        updateNotesRequest: NotesUpdateRequest
    ): LeadNotesResponse
    fun deleteNotesById(leadId: UUID, taskId: UUID?, notesId: UUID)
    fun getAllNotes(paginationRequest: PaginationRequest): PaginatedResponse<List<LeadNotesResponse>>
}
