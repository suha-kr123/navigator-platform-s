package com.nivasafinance.features.lead.service.impl

import base.model.PaginatedResponse
import base.model.PaginationInfo
import base.model.PaginationRequest
import com.nivasafinance.features.lead.dto.LeadTaskNotesResponse
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
    private fun NotesResponse.toLeadTaskNotesResponse(leadId: UUID, taskId: UUID): LeadTaskNotesResponse {
        return LeadTaskNotesResponse(
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

    override fun addNotesToLead(leadId: UUID, taskId: UUID, addNotesToLeadRequest: NotesRequest): LeadTaskNotesResponse {
        // Verify lead exists
        val lead = leadRepositoryWrapper.findByIdWithException(leadId)

        // Verify that the taskId belongs to this lead
        val taskExists = lead.taskData?.any { it.taskId == taskId } ?: false
        if (!taskExists) {
            throw IllegalArgumentException("Task with ID $taskId does not belong to lead $leadId")
        }

        // Create notes using NotesService
        val notesResponse = notesService.createNotes(addNotesToLeadRequest)

        // Update the lead's task data to include the notes ID
        val currentTaskData = lead.taskData ?: emptyList()
        val updatedTaskData = currentTaskData.map { taskData ->
            if (taskData.taskId == taskId) {
                val currentNotesIds = taskData.notesIds ?: emptyList()
                taskData.copy(notesIds = currentNotesIds + notesResponse.id)
            } else {
                taskData
            }
        }

        // Update the lead entity directly instead of using copy() to preserve version
        lead.taskData = updatedTaskData
        leadRepositoryWrapper.saveWithException(lead)

        return notesResponse.toLeadTaskNotesResponse(leadId, taskId)
    }

    override fun getTaskNotes(leadId: UUID, taskId: UUID, paginationRequest: PaginationRequest): PaginatedResponse<List<LeadTaskNotesResponse>> {
        // Verify lead exists
        val lead = leadRepositoryWrapper.findByIdWithException(leadId)

        // Verify that the taskId belongs to this lead
        val taskExists = lead.taskData?.any { it.taskId == taskId } ?: false
        if (!taskExists) {
            throw IllegalArgumentException("Task with ID $taskId does not belong to lead $leadId")
        }

        // Get notes for the specific task
        val taskData = lead.taskData?.find { it.taskId == taskId }
        val notesIds = taskData?.notesIds ?: emptyList()

        // Convert to LeadTaskNotesResponse
        val leadTaskNotesResponses = notesIds.map { notesId ->
            val notes = notesService.getNotesById(notesId)
            notes.toLeadTaskNotesResponse(leadId, taskId)
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

    override fun getNotesById(leadId: UUID, taskId: UUID, notesId: UUID): LeadTaskNotesResponse {
        // Verify lead exists and contains the notes
        val lead = leadRepositoryWrapper.findByIdWithException(leadId)

        // Verify that the taskId belongs to this lead
        val taskExists = lead.taskData?.any { it.taskId == taskId } ?: false
        if (!taskExists) {
            throw IllegalArgumentException("Task with ID $taskId does not belong to lead $leadId")
        }

        val notesExists = lead.taskData?.any { taskData ->
            taskData.taskId == taskId && taskData.notesIds?.contains(notesId) == true
        } ?: false

        if (!notesExists) {
            throw IllegalArgumentException("Notes with ID $notesId does not belong to task $taskId in lead $leadId")
        }

        val notes = notesService.getNotesById(notesId)
        return notes.toLeadTaskNotesResponse(leadId, taskId)
    }

    override fun getLeadNotes(leadId: UUID, paginationRequest: PaginationRequest): PaginatedResponse<List<LeadTaskNotesResponse>> {
        // Verify lead exists
        val lead = leadRepositoryWrapper.findByIdWithException(leadId)

        // Get all notes IDs from the lead's task data
        val allNotesIds = lead.taskData?.flatMap { taskData ->
            taskData.notesIds ?: emptyList()
        } ?: emptyList()

        if (allNotesIds.isEmpty()) {
            return PaginatedResponse(
                content = listOf(emptyList()),
                pagination = PaginationInfo(
                    offset = paginationRequest.offset,
                    limit = paginationRequest.limit,
                    totalElements = 0,
                    totalPages = 0,
                    currentPage = 0,
                    hasNext = false,
                    hasPrevious = false
                )
            )
        }

        // Get all notes and filter by the notesIds from the lead
        val allNotes = notesService.getAllNotes(paginationRequest)
        val filteredNotes = allNotes.content.filter { notes ->
            allNotesIds.contains(notes.id)
        }

        // Convert to LeadTaskNotesResponse with lead ID and task ID
        val leadTaskNotesResponses = filteredNotes.map { notes ->
            // We need to find which task this notes belongs to
            val taskData = lead.taskData?.find { it.notesIds?.contains(notes.id) == true }
            val taskId = taskData?.taskId ?: UUID.randomUUID() // fallback if not found
            notes.toLeadTaskNotesResponse(leadId, taskId)
        }

        // Create a new paginated response with filtered notes
        return PaginatedResponse(
            content = listOf(leadTaskNotesResponses),
            pagination = PaginationInfo(
                offset = paginationRequest.offset,
                limit = paginationRequest.limit,
                totalElements = leadTaskNotesResponses.size.toLong(),
                totalPages = if (leadTaskNotesResponses.isEmpty()) 0 else 1,
                currentPage = 0,
                hasNext = false,
                hasPrevious = false
            )
        )
    }

    override fun getAllLeadsNotes(paginationRequest: PaginationRequest): PaginatedResponse<List<LeadTaskNotesResponse>> {
        // Get all leads that have notes
        val pageable = org.springframework.data.domain.PageRequest.of(
            paginationRequest.offset / paginationRequest.limit,
            paginationRequest.limit
        )
        val leadsPage = leadRepositoryWrapper.findAllWithException(pageable)

        val allLeadTaskNotesResponses = mutableListOf<LeadTaskNotesResponse>()

        leadsPage.content.forEach { lead ->
            lead.taskData?.forEach { taskData ->
                val notesIds = taskData.notesIds ?: emptyList()
                notesIds.forEach { notesId ->
                    try {
                        val notes = notesService.getNotesById(notesId)
                        allLeadTaskNotesResponses.add(
                            notes.toLeadTaskNotesResponse(lead.id!!, taskData.taskId)
                        )
                    } catch (e: Exception) {
                        // Skip notes that no longer exist
                    }
                }
            }
        }

        val totalPages = if (allLeadTaskNotesResponses.isEmpty()) 0 else ((allLeadTaskNotesResponses.size - 1) / paginationRequest.limit + 1)
        val currentPage = paginationRequest.offset / paginationRequest.limit

        return PaginatedResponse(
            content = listOf(allLeadTaskNotesResponses),
            pagination = PaginationInfo(
                offset = paginationRequest.offset,
                limit = paginationRequest.limit,
                totalElements = allLeadTaskNotesResponses.size.toLong(),
                totalPages = totalPages,
                currentPage = currentPage,
                hasNext = currentPage < totalPages - 1,
                hasPrevious = currentPage > 0
            )
        )
    }

    override fun updateNotesById(leadId: UUID, taskId: UUID, notesId: UUID, updateNotesRequest: NotesUpdateRequest): LeadTaskNotesResponse {
        // Verify lead exists and contains the notes
        val lead = leadRepositoryWrapper.findByIdWithException(leadId)

        // Verify that the taskId belongs to this lead
        val taskExists = lead.taskData?.any { it.taskId == taskId } ?: false
        if (!taskExists) {
            throw IllegalArgumentException("Task with ID $taskId does not belong to lead $leadId")
        }

        val notesExists = lead.taskData?.any { taskData ->
            taskData.taskId == taskId && taskData.notesIds?.contains(notesId) == true
        } ?: false

        if (!notesExists) {
            throw IllegalArgumentException("Notes with ID $notesId does not belong to task $taskId in lead $leadId")
        }

        val updatedNotes = notesService.updateNotes(notesId, updateNotesRequest)
        return updatedNotes.toLeadTaskNotesResponse(leadId, taskId)
    }

    override fun patchNotesById(leadId: UUID, taskId: UUID, notesId: UUID, updateNotesRequest: NotesUpdateRequest): LeadTaskNotesResponse {
        // Verify lead exists and contains the notes
        val lead = leadRepositoryWrapper.findByIdWithException(leadId)

        // Verify that the taskId belongs to this lead
        val taskExists = lead.taskData?.any { it.taskId == taskId } ?: false
        if (!taskExists) {
            throw IllegalArgumentException("Task with ID $taskId does not belong to lead $leadId")
        }

        val notesExists = lead.taskData?.any { taskData ->
            taskData.taskId == taskId && taskData.notesIds?.contains(notesId) == true
        } ?: false

        if (!notesExists) {
            throw IllegalArgumentException("Notes with ID $notesId does not belong to task $taskId in lead $leadId")
        }

        val updatedNotes = notesService.patchNotes(notesId, updateNotesRequest)
        return updatedNotes.toLeadTaskNotesResponse(leadId, taskId)
    }

    override fun deleteNotesById(leadId: UUID, taskId: UUID, notesId: UUID) {
        // Verify lead exists and contains the notes
        val lead = leadRepositoryWrapper.findByIdWithException(leadId)

        // Verify that the taskId belongs to this lead
        val taskExists = lead.taskData?.any { it.taskId == taskId } ?: false
        if (!taskExists) {
            throw IllegalArgumentException("Task with ID $taskId does not belong to lead $leadId")
        }

        val notesExists = lead.taskData?.any { taskData ->
            taskData.taskId == taskId && taskData.notesIds?.contains(notesId) == true
        } ?: false

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
        leadRepositoryWrapper.saveWithException(lead)

        // Delete the notes
        notesService.deleteNotes(notesId)
    }
}