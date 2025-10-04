package com.nivasafinance.features.lead.service.impl

import com.nivasafinance.common.base.model.PaginatedResponse
import com.nivasafinance.common.base.model.PaginationRequest
import com.nivasafinance.common.base.model.toBasicPaginatedResponse
import com.nivasafinance.features.lead.dto.LeadNotesResponse
import com.nivasafinance.features.lead.dto.toLeadNotesResponse
import com.nivasafinance.features.lead.entity.LeadNotesData
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper
import com.nivasafinance.features.lead.service.LeadNotesService
import com.nivasafinance.features.lead.utils.toLeadNoteResponse
import com.nivasafinance.features.notes.dto.NotesRequest
import com.nivasafinance.features.notes.dto.NotesUpdateRequest
import com.nivasafinance.features.notes.service.NotesService
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class LeadNotesServiceImpl(
    private val notesService: NotesService,
    private val leadRepositoryWrapper: LeadRepositoryWrapper,
    private val messageSource: MessageSource
) : LeadNotesService {
    @Transactional
    override fun addNotesToLead(
        leadId: UUID,
        taskId: UUID?,
        addNotesToLeadRequest: NotesRequest
    ): LeadNotesResponse {
        val lead = leadRepositoryWrapper.findByIdWithException(leadId)
        val noteResponse = notesService.createNotes(notesRequest = addNotesToLeadRequest)
        val updatedNotes = lead.notes?.toMutableList() ?: mutableListOf()
        updatedNotes.add(LeadNotesData(noteId = noteResponse.id, taskId = taskId, createdDate = noteResponse.createdAt))
        lead.notes = updatedNotes
        leadRepositoryWrapper.saveWithException(lead)
        return noteResponse.toLeadNotesResponse(leadId, null)
    }

    override fun getLeadNotes(
        leadId: UUID,
        paginationRequest: PaginationRequest
    ): PaginatedResponse<LeadNotesResponse> {
        val lead = leadRepositoryWrapper.findByIdWithException(leadId)
        return lead.notes
            .orEmpty()
            .sortedByDescending { it.createdDate }
            .map { it.toLeadNoteResponse(leadId, notesService) }
            .toBasicPaginatedResponse()
    }

    override fun getTaskNotes(
        leadId: UUID,
        taskId: UUID,
        paginationRequest: PaginationRequest
    ): PaginatedResponse<LeadNotesResponse> {
        val lead = leadRepositoryWrapper.findByIdWithException(leadId)
        return lead.notes
            .orEmpty()
            .filter { it.taskId == taskId }
            .sortedByDescending { it.createdDate }
            .map { it.toLeadNoteResponse(leadId, notesService) }
            .toBasicPaginatedResponse()
    }

    @Transactional
    override fun patchNotesById(
        leadId: UUID,
        notesId: UUID,
        updateNotesRequest: NotesUpdateRequest
    ) {
        leadRepositoryWrapper.findByIdWithException(leadId)
        notesService.updateNotes(notesId, updateNotesRequest)
    }

    @Transactional
    override fun deleteNotesById(leadId: UUID, notesId: UUID) {
        val lead = leadRepositoryWrapper.findByIdWithException(leadId)
        notesService.deleteNotes(notesId)
        lead.notes = lead.notes?.filter { it.noteId != notesId }
        leadRepositoryWrapper.saveWithException(lead)
    }

}
