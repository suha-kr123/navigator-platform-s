package com.nivasafinance.features.lead.service

import base.model.PaginatedResponse
import base.model.PaginationRequest
import com.nivasafinance.features.lead.dto.LeadNotesResponse
import com.nivasafinance.features.notes.dto.NotesRequest
import com.nivasafinance.features.notes.dto.NotesUpdateRequest
import java.util.UUID

interface LeadNotesService {
    fun addNotesToLead(leadId: UUID, taskId: UUID?, addNotesToLeadRequest: NotesRequest): LeadNotesResponse

    fun getLeadNotes(leadId: UUID, paginationRequest: PaginationRequest): PaginatedResponse<LeadNotesResponse>

    fun getTaskNotes(
        leadId: UUID,
        taskId: UUID,
        paginationRequest: PaginationRequest
    ): PaginatedResponse<LeadNotesResponse>

    fun patchNotesById(
        leadId: UUID,
        notesId: UUID,
        updateNotesRequest: NotesUpdateRequest
    )

    fun deleteNotesById(leadId: UUID, notesId: UUID)
}
