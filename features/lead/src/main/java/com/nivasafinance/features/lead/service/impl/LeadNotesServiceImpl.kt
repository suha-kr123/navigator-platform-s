package com.nivasafinance.features.lead.service.impl

import base.model.PaginatedResponse
import base.model.PaginationInfo
import base.model.PaginationRequest
import com.nivasafinance.features.lead.dto.LeadNotesResponse
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper
import com.nivasafinance.features.lead.service.LeadNotesService
import com.nivasafinance.features.lead.exception.LeadExceptionFactory
import com.nivasafinance.features.notes.dto.NotesRequest
import com.nivasafinance.features.notes.dto.NotesResponse
import com.nivasafinance.features.notes.dto.NotesUpdateRequest
import com.nivasafinance.features.notes.service.NotesService
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class LeadNotesServiceImpl(
    private val notesService: NotesService,
    private val leadRepositoryWrapper: LeadRepositoryWrapper,
    private val messageSource: MessageSource
) : LeadNotesService {

    private fun NotesResponse.toLeadNotesResponse(leadId: UUID, taskId: UUID?): LeadNotesResponse {
        return LeadNotesResponse(
            leadId = leadId,
            taskId = taskId,
            noteId = this.id,
            noteTitle = this.title,
            noteContent = this.content,
            noteCreatedAt = this.createdAt,
            noteCreatedBy = this.createdBy ?: "system",
            noteUpdatedAt = this.updatedAt,
            noteUpdatedBy = this.updatedBy ?: "system"
        )
    }

    override fun addNotesToLead(leadId: UUID, taskId: UUID?, addNotesToLeadRequest: NotesRequest): LeadNotesResponse {
        val lead = leadRepositoryWrapper.findByIdWithException(leadId)

        if (taskId != null) {
            val taskExists = lead.taskData?.any { it.taskId == taskId } ?: false
            if (!taskExists) {
                throw LeadExceptionFactory.taskNotBelongsToLead(taskId, leadId, messageSource)
            }
        }

        val notesResponse = notesService.createNotes(addNotesToLeadRequest)

        if (taskId != null) {
            val currentTaskData = lead.taskData ?: emptyList()
            val updatedTaskData = currentTaskData.map { taskData ->
                if (taskData.taskId == taskId) {
                    val currentNotesIds = taskData.notesIds
                    taskData.copy(notesIds = currentNotesIds + notesResponse.id)
                } else {
                    taskData
                }
            }
            lead.taskData = updatedTaskData
        }

        val currentNoteIds = lead.noteIds ?: emptyList()
        lead.noteIds = (currentNoteIds + notesResponse.id).distinct()

        leadRepositoryWrapper.saveWithException(lead)

        return notesResponse.toLeadNotesResponse(leadId, taskId)
    }

    override fun getTaskNotes(leadId: UUID, taskId: UUID?, paginationRequest: PaginationRequest): PaginatedResponse<List<LeadNotesResponse>> {
        val lead = leadRepositoryWrapper.findByIdWithException(leadId)

        if (taskId != null) {
            val taskExists = lead.taskData?.any { it.taskId == taskId } ?: false
            if (!taskExists) {
                throw LeadExceptionFactory.taskNotBelongsToLead(taskId, leadId, messageSource)
            }
        }

        val notesIds = if (taskId != null) {
            val taskData = lead.taskData?.find { it.taskId == taskId }
            taskData?.notesIds ?: emptyList()
        } else {
            lead.noteIds ?: emptyList()
        }

        val leadTaskNotesResponses = notesIds.map { notesId ->
            val notes = notesService.getNotesById(notesId)
            notes.toLeadNotesResponse(leadId, taskId)
        }

        return PaginatedResponse(
            content = listOf(leadTaskNotesResponses),
            pagination = PaginationInfo(
                offset = paginationRequest.offset,
                limit = paginationRequest.limit,
                totalElements = leadTaskNotesResponses.size.toLong(),
                totalPages = 1,
                currentPage = 1,
                hasNext = false,
                hasPrevious = false
            )
        )
    }

    override fun getNotesById(leadId: UUID, taskId: UUID?, notesId: UUID): LeadNotesResponse {
        val lead = leadRepositoryWrapper.findByIdWithException(leadId)

        if (taskId != null) {
            val taskExists = lead.taskData?.any { it.taskId == taskId } ?: false
            if (!taskExists) {
                throw LeadExceptionFactory.taskNotBelongsToLead(taskId, leadId, messageSource)
            }
        }

        val notesExists = if (taskId != null) {
            lead.taskData?.any { taskData ->
                taskData.taskId == taskId && taskData.notesIds.contains(notesId)
            } ?: false
        } else {
            val existsInTasks = lead.taskData?.any { taskData ->
                taskData.notesIds.contains(notesId)
            } ?: false
            val existsInLeadNotes = lead.noteIds?.contains(notesId) ?: false
            existsInTasks || existsInLeadNotes
        }

        if (!notesExists) {
            throw LeadExceptionFactory.notesNotBelongsToTask(notesId, taskId, leadId, messageSource)
        }

        val notes = notesService.getNotesById(notesId)
        return notes.toLeadNotesResponse(leadId, taskId)
    }

    override fun updateNotesById(leadId: UUID, taskId: UUID?, notesId: UUID, updateNotesRequest: NotesUpdateRequest): LeadNotesResponse {
        val lead = leadRepositoryWrapper.findByIdWithException(leadId)

        if (taskId != null) {
            val taskExists = lead.taskData?.any { it.taskId == taskId } ?: false
            if (!taskExists) {
                throw LeadExceptionFactory.taskNotBelongsToLead(taskId, leadId, messageSource)
            }
        }

        val notesExists = if (taskId != null) {
            lead.taskData?.any { taskData ->
                taskData.taskId == taskId && taskData.notesIds.contains(notesId)
            } ?: false
        } else {
            val existsInTasks = lead.taskData?.any { taskData ->
                taskData.notesIds.contains(notesId)
            } ?: false
            val existsInLeadNotes = lead.noteIds?.contains(notesId) ?: false
            existsInTasks || existsInLeadNotes
        }

        if (!notesExists) {
            throw LeadExceptionFactory.notesNotBelongsToTask(notesId, taskId, leadId, messageSource)
        }

        val updatedNotes = notesService.updateNotes(notesId, updateNotesRequest)
        return updatedNotes.toLeadNotesResponse(leadId, taskId)
    }

    override fun patchNotesById(leadId: UUID, taskId: UUID?, notesId: UUID, updateNotesRequest: NotesUpdateRequest): LeadNotesResponse {
        val lead = leadRepositoryWrapper.findByIdWithException(leadId)

        if (taskId != null) {
            val taskExists = lead.taskData?.any { it.taskId == taskId } ?: false
            if (!taskExists) {
                throw LeadExceptionFactory.taskNotBelongsToLead(taskId, leadId, messageSource)
            }
        }

        val notesExists = if (taskId != null) {
            lead.taskData?.any { taskData ->
                taskData.taskId == taskId && taskData.notesIds.contains(notesId)
            } ?: false
        } else {
            val existsInTasks = lead.taskData?.any { taskData ->
                taskData.notesIds.contains(notesId)
            } ?: false
            val existsInLeadNotes = lead.noteIds?.contains(notesId) ?: false
            existsInTasks || existsInLeadNotes
        }

        if (!notesExists) {
            throw LeadExceptionFactory.notesNotBelongsToTask(notesId, taskId, leadId, messageSource)
        }

        val updatedNotes = notesService.patchNotes(notesId, updateNotesRequest)
        return updatedNotes.toLeadNotesResponse(leadId, taskId)
    }

    override fun deleteNotesById(leadId: UUID, taskId: UUID?, notesId: UUID) {
        val lead = leadRepositoryWrapper.findByIdWithException(leadId)

        if (taskId != null) {
            val taskExists = lead.taskData?.any { it.taskId == taskId } ?: false
            if (!taskExists) {
                throw LeadExceptionFactory.taskNotBelongsToLead(taskId, leadId, messageSource)
            }
        }

        val notesExists = if (taskId != null) {
            lead.taskData?.any { taskData ->
                taskData.taskId == taskId && taskData.notesIds.contains(notesId)
            } ?: false
        } else {
            val existsInTasks = lead.taskData?.any { taskData ->
                taskData.notesIds.contains(notesId)
            } ?: false
            val existsInLeadNotes = lead.noteIds?.contains(notesId) ?: false
            existsInTasks || existsInLeadNotes
        }

        if (!notesExists) {
            throw LeadExceptionFactory.notesNotBelongsToTask(notesId, taskId, leadId, messageSource)
        }

        val updatedTaskData = lead.taskData?.map { taskData ->
            val updatedNotesIds = taskData.notesIds.filter { it != notesId }
            taskData.copy(notesIds = updatedNotesIds)
        } ?: emptyList()

        lead.taskData = updatedTaskData

        val currentNoteIds = lead.noteIds ?: emptyList()
        lead.noteIds = currentNoteIds.filter { it != notesId }

        leadRepositoryWrapper.saveWithException(lead)

        notesService.deleteNotes(notesId)
    }

    override fun getAllNotes(paginationRequest: PaginationRequest): PaginatedResponse<List<LeadNotesResponse>> {
        return PaginatedResponse(
            content = emptyList(),
            pagination = PaginationInfo(
                offset = paginationRequest.offset,
                limit = paginationRequest.limit,
                totalElements = 0,
                totalPages = 0,
                currentPage = 1,
                hasNext = false,
                hasPrevious = false
            )
        )
    }
}
