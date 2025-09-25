package com.nivasafinance.features.lead.service.impl

import base.model.PaginatedResponse
import base.model.PaginationInfo
import base.model.PaginationRequest
import com.nivasafinance.features.lead.dto.LeadNotesResponse
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper
import com.nivasafinance.features.lead.service.LeadNotesService
import com.nivasafinance.features.notes.dto.NotesRequest
import com.nivasafinance.features.notes.dto.NotesResponse
import com.nivasafinance.features.notes.dto.NotesUpdateRequest
import com.nivasafinance.features.notes.service.NotesService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class LeadNotesServiceImpl(
    private val notesService: NotesService,
    private val leadRepositoryWrapper: LeadRepositoryWrapper
) : LeadNotesService {

    /**
     * Converts NotesResponse to LeadTaskNotesResponse
     */
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
        // Verify lead exists
        val lead = leadRepositoryWrapper.findByIdWithException(leadId)

        // If taskId is provided, verify that it belongs to this lead
        if (taskId != null) {
            val taskExists = lead.taskData?.any { it.taskId == taskId } ?: false
            if (!taskExists) {
                throw IllegalArgumentException("Task with ID $taskId does not belong to lead $leadId")
            }
        }

        // Create notes using NotesService
        val notesResponse = notesService.createNotes(addNotesToLeadRequest)

        // Update the lead's task data to include the notes ID (only if taskId is provided)
        if (taskId != null) {
            val currentTaskData = lead.taskData ?: emptyList()
            val updatedTaskData = currentTaskData.map { taskData ->
                if (taskData.taskId == taskId) {
                    val currentNotesIds = taskData.notesIds ?: emptyList()
                    taskData.copy(notesIds = currentNotesIds + notesResponse.id)
                } else {
                    taskData
                }
            }
            lead.taskData = updatedTaskData
        }

        // Also update the lead's noteIds list
        val currentNoteIds = lead.noteIds ?: emptyList()
        lead.noteIds = (currentNoteIds + notesResponse.id).distinct()

        leadRepositoryWrapper.saveWithException(lead)

        return notesResponse.toLeadNotesResponse(leadId, taskId)
    }

    override fun getTaskNotes(leadId: UUID, taskId: UUID?, paginationRequest: PaginationRequest): PaginatedResponse<List<LeadNotesResponse>> {
        // Verify lead exists
        val lead = leadRepositoryWrapper.findByIdWithException(leadId)

        // If taskId is provided, verify that it belongs to this lead
        if (taskId != null) {
            val taskExists = lead.taskData?.any { it.taskId == taskId } ?: false
            if (!taskExists) {
                throw IllegalArgumentException("Task with ID $taskId does not belong to lead $leadId")
            }
        }

        // Get notes for the specific task or all lead notes
        val notesIds = if (taskId != null) {
            val taskData = lead.taskData?.find { it.taskId == taskId }
            taskData?.notesIds ?: emptyList()
        } else {
            lead.noteIds ?: emptyList()
        }

        // Convert to LeadTaskNotesResponse
        val leadTaskNotesResponses = notesIds.map { notesId ->
            val notes = notesService.getNotesById(notesId)
            notes.toLeadNotesResponse(leadId, taskId)
        }

        // For now, return all notes without pagination
        // In a real implementation, you might want to implement proper pagination
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
        // Verify lead exists and contains the notes
        val lead = leadRepositoryWrapper.findByIdWithException(leadId)

        // If taskId is provided, verify that it belongs to this lead
        if (taskId != null) {
            val taskExists = lead.taskData?.any { it.taskId == taskId } ?: false
            if (!taskExists) {
                throw IllegalArgumentException("Task with ID $taskId does not belong to lead $leadId")
            }
        }

        // Check if notes exists in the lead's task data or general notes list
        val notesExists = if (taskId != null) {
            // Check specific task
            lead.taskData?.any { taskData ->
                taskData.taskId == taskId && taskData.notesIds?.contains(notesId) == true
            } ?: false
        } else {
            // Check all tasks or lead's general notes list
            val existsInTasks = lead.taskData?.any { taskData ->
                taskData.notesIds?.contains(notesId) == true
            } ?: false
            val existsInLeadNotes = lead.noteIds?.contains(notesId) ?: false
            existsInTasks || existsInLeadNotes
        }

        if (!notesExists) {
            throw IllegalArgumentException("Notes with ID $notesId does not belong to task $taskId in lead $leadId")
        }

        val notes = notesService.getNotesById(notesId)
        return notes.toLeadNotesResponse(leadId, taskId)
    }

    override fun updateNotesById(leadId: UUID, taskId: UUID?, notesId: UUID, updateNotesRequest: NotesUpdateRequest): LeadNotesResponse {
        // Verify lead exists and contains the notes
        val lead = leadRepositoryWrapper.findByIdWithException(leadId)

        // If taskId is provided, verify that it belongs to this lead
        if (taskId != null) {
            val taskExists = lead.taskData?.any { it.taskId == taskId } ?: false
            if (!taskExists) {
                throw IllegalArgumentException("Task with ID $taskId does not belong to lead $leadId")
            }
        }

        // Check if notes exists in the lead's task data or general notes list
        val notesExists = if (taskId != null) {
            // Check specific task
            lead.taskData?.any { taskData ->
                taskData.taskId == taskId && taskData.notesIds?.contains(notesId) == true
            } ?: false
        } else {
            // Check all tasks or lead's general notes list
            val existsInTasks = lead.taskData?.any { taskData ->
                taskData.notesIds?.contains(notesId) == true
            } ?: false
            val existsInLeadNotes = lead.noteIds?.contains(notesId) ?: false
            existsInTasks || existsInLeadNotes
        }

        if (!notesExists) {
            throw IllegalArgumentException("Notes with ID $notesId does not belong to task $taskId in lead $leadId")
        }

        val updatedNotes = notesService.updateNotes(notesId, updateNotesRequest)
        return updatedNotes.toLeadNotesResponse(leadId, taskId)
    }

    override fun patchNotesById(leadId: UUID, taskId: UUID?, notesId: UUID, updateNotesRequest: NotesUpdateRequest): LeadNotesResponse {
        // Verify lead exists and contains the notes
        val lead = leadRepositoryWrapper.findByIdWithException(leadId)

        // If taskId is provided, verify that it belongs to this lead
        if (taskId != null) {
            val taskExists = lead.taskData?.any { it.taskId == taskId } ?: false
            if (!taskExists) {
                throw IllegalArgumentException("Task with ID $taskId does not belong to lead $leadId")
            }
        }

        // Check if notes exists in the lead's task data or general notes list
        val notesExists = if (taskId != null) {
            // Check specific task
            lead.taskData?.any { taskData ->
                taskData.taskId == taskId && taskData.notesIds?.contains(notesId) == true
            } ?: false
        } else {
            // Check all tasks or lead's general notes list
            val existsInTasks = lead.taskData?.any { taskData ->
                taskData.notesIds?.contains(notesId) == true
            } ?: false
            val existsInLeadNotes = lead.noteIds?.contains(notesId) ?: false
            existsInTasks || existsInLeadNotes
        }

        if (!notesExists) {
            throw IllegalArgumentException("Notes with ID $notesId does not belong to task $taskId in lead $leadId")
        }

        val updatedNotes = notesService.patchNotes(notesId, updateNotesRequest)
        return updatedNotes.toLeadNotesResponse(leadId, taskId)
    }

    override fun deleteNotesById(leadId: UUID, taskId: UUID?, notesId: UUID) {
        // Verify lead exists and contains the notes
        val lead = leadRepositoryWrapper.findByIdWithException(leadId)

        // If taskId is provided, verify that it belongs to this lead
        if (taskId != null) {
            val taskExists = lead.taskData?.any { it.taskId == taskId } ?: false
            if (!taskExists) {
                throw IllegalArgumentException("Task with ID $taskId does not belong to lead $leadId")
            }
        }

        // Check if notes exists in the lead's task data or general notes list
        val notesExists = if (taskId != null) {
            // Check specific task
            lead.taskData?.any { taskData ->
                taskData.taskId == taskId && taskData.notesIds?.contains(notesId) == true
            } ?: false
        } else {
            // Check all tasks or lead's general notes list
            val existsInTasks = lead.taskData?.any { taskData ->
                taskData.notesIds?.contains(notesId) == true
            } ?: false
            val existsInLeadNotes = lead.noteIds?.contains(notesId) ?: false
            existsInTasks || existsInLeadNotes
        }

        if (!notesExists) {
            throw IllegalArgumentException("Notes with ID $notesId does not belong to task $taskId in lead $leadId")
        }

        // Remove notes from lead's task data
        val updatedTaskData = lead.taskData?.map { taskData ->
            val updatedNotesIds = taskData.notesIds?.filter { it != notesId } ?: emptyList()
            taskData.copy(notesIds = updatedNotesIds)
        } ?: emptyList()

        // Update the lead entity directly instead of using copy() to preserve version
        lead.taskData = updatedTaskData

        // Also remove from lead's noteIds list
        val currentNoteIds = lead.noteIds ?: emptyList()
        lead.noteIds = currentNoteIds.filter { it != notesId }

        leadRepositoryWrapper.saveWithException(lead)

        // Delete the notes
        notesService.deleteNotes(notesId)
    }

    override fun getAllNotes(paginationRequest: PaginationRequest): PaginatedResponse<List<LeadNotesResponse>> {
        // This would typically query all notes across all leads
        // For now, return empty list - implement based on your requirements
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
