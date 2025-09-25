package com.nivasafinance.features.lead.service

import base.model.PaginatedResponse
import base.model.PaginationRequest
import com.nivasafinance.features.lead.dto.LeadTaskNotesResponse
import com.nivasafinance.features.notes.dto.NotesRequest
import com.nivasafinance.features.notes.dto.NotesUpdateRequest
import java.util.UUID

interface LeadNotesService {
    fun addNotesToLead(leadId: UUID, taskId: UUID, addNotesToLeadRequest: NotesRequest): LeadTaskNotesResponse
    fun getTaskNotes(leadId: UUID, taskId: UUID, paginationRequest: PaginationRequest): PaginatedResponse<LeadTaskNotesResponse>
    fun getNotesById(leadId: UUID, taskId: UUID, notesId: UUID): LeadTaskNotesResponse
    fun getLeadNotes(leadId: UUID, paginationRequest: PaginationRequest): PaginatedResponse<LeadTaskNotesResponse>
    fun getAllLeadsNotes(paginationRequest: PaginationRequest): PaginatedResponse<LeadTaskNotesResponse>
    fun updateNotesById(leadId: UUID, taskId: UUID, notesId: UUID, updateNotesRequest: NotesUpdateRequest): LeadTaskNotesResponse
    fun patchNotesById(leadId: UUID, taskId: UUID, notesId: UUID, updateNotesRequest: NotesUpdateRequest): LeadTaskNotesResponse
    fun deleteNotesById(leadId: UUID, taskId: UUID, notesId: UUID)
}