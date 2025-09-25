package com.nivasafinance.features.lead.service.impl

import base.model.PaginatedResponse
import base.model.PaginationInfo
import base.model.PaginationRequest
import com.nivasafinance.features.lead.dto.LeadTasksResponse
import com.nivasafinance.features.lead.entity.Lead
import com.nivasafinance.features.lead.entity.TaskData
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper
import com.nivasafinance.features.lead.service.LeadTaskService
import com.nivasafinance.features.tasks.dto.TaskRequest
import com.nivasafinance.features.tasks.dto.TaskResponse
import com.nivasafinance.features.tasks.dto.UpdateTaskRequest
import com.nivasafinance.features.tasks.service.TaskService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class LeadTaskServiceImpl(
    private val leadRepositoryWrapper: LeadRepositoryWrapper,
    private val taskService: TaskService
) : LeadTaskService {

    private val logger = LoggerFactory.getLogger(LeadTaskServiceImpl::class.java)

    override fun getAllTasks(paginationRequest: PaginationRequest): PaginatedResponse<LeadTasksResponse> {
        return try {
            // Get all tasks from TaskService
            val allTasks = taskService.getAllTasks(paginationRequest)

            // Get all leads to find which lead each task belongs to
            // We need to get all leads, so we'll use a large page size
            val pageable = org.springframework.data.domain.PageRequest.of(0, 10_000)
            val allLeadsPage = leadRepositoryWrapper.findAllWithException(pageable)
            val allLeads = allLeadsPage.content

            // Create a map of taskId -> leadId for quick lookup
            val taskToLeadMap = mutableMapOf<UUID, UUID>()
            allLeads.forEach { lead ->
                lead.taskData?.forEach { taskData ->
                    taskToLeadMap[taskData.taskId] = lead.id!!
                }
            }

            // Convert to LeadTasksResponse with actual lead IDs
            // Filter out tasks that are not associated with any lead to handle data integrity gracefully
            val tasksWithoutLead = allTasks.content.filter { task ->
                !taskToLeadMap.containsKey(task.id)
            }
            if (tasksWithoutLead.isNotEmpty()) {
                logger.warn(
                    "Found ${tasksWithoutLead.size} tasks not associated with any lead. Task IDs: ${tasksWithoutLead.map { it.id }}"
                )
            }

            val leadTasksResponses = allTasks.content
                .filter { task -> taskToLeadMap.containsKey(task.id) }
                .map { task ->
                    val leadId = taskToLeadMap[task.id]!!
                    LeadTasksResponse(
                        leadId = leadId,
                        tasks = listOf(task)
                    )
                }

            // Update pagination to reflect the filtered results
            val filteredPagination = PaginationInfo(
                offset = paginationRequest.offset,
                limit = paginationRequest.limit,
                totalElements = leadTasksResponses.size.toLong(),
                totalPages = if (leadTasksResponses.isEmpty()) 0 else
                    ((leadTasksResponses.size - 1) / paginationRequest.limit + 1),
                currentPage = paginationRequest.offset / paginationRequest.limit,
                hasNext = (paginationRequest.offset + paginationRequest.limit) <
                    leadTasksResponses.size,
                hasPrevious = paginationRequest.offset > 0
            )

            PaginatedResponse(
                content = leadTasksResponses,
                pagination = filteredPagination
            )
        } catch (e: Exception) {
            throw IllegalStateException("Failed to retrieve all tasks: ${e.message}", e)
        }
    }

    override fun getLeadTasks(leadId: UUID, paginationRequest: PaginationRequest): PaginatedResponse<LeadTasksResponse> {
        return try {
            // Verify lead exists
            val lead = leadRepositoryWrapper.findByIdWithException(leadId)

            // Get task data from lead
            val taskData = lead.taskData ?: emptyList()

            // Convert to LeadTasksResponse with proper error handling
            val leadTasksResponses = taskData.map { taskDataItem ->
                val task = try {
                    taskService.getTaskById(taskDataItem.taskId)
                } catch (e: Exception) {
                    throw IllegalStateException(
                        "Failed to retrieve task ${taskDataItem.taskId} for lead $leadId: ${e.message}",
                        e
                    )
                }
                LeadTasksResponse(
                    leadId = leadId,
                    tasks = listOf(task)
                )
            }

            // Implement proper pagination for lead tasks
            val totalElements = leadTasksResponses.size.toLong()
            val totalPages = if (totalElements == 0L) 0 else ((totalElements - 1) / paginationRequest.limit + 1).toInt()
            val currentPage = paginationRequest.offset / paginationRequest.limit

            // Apply pagination to the results
            val paginatedContent = leadTasksResponses
                .drop(paginationRequest.offset)
                .take(paginationRequest.limit)

            PaginatedResponse(
                content = paginatedContent,
                pagination = PaginationInfo(
                    offset = paginationRequest.offset,
                    limit = paginationRequest.limit,
                    totalElements = totalElements,
                    totalPages = totalPages,
                    currentPage = currentPage,
                    hasNext = currentPage < totalPages - 1,
                    hasPrevious = currentPage > 0
                )
            )
        } catch (e: Exception) {
            throw IllegalStateException("Failed to retrieve tasks for lead $leadId: ${e.message}", e)
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun createTaskForLead(leadId: UUID, createTaskRequest: TaskRequest): LeadTasksResponse {
        return try {
            // Verify lead exists
            val lead = leadRepositoryWrapper.findByIdWithException(leadId)

            // Create task using TaskService with proper error handling
            val taskResponse = try {
                taskService.createTask(createTaskRequest)
            } catch (e: Exception) {
                throw IllegalStateException("Failed to create task for lead $leadId: ${e.message}", e)
            }

            // Add task to lead's task data
            val currentTaskData = lead.taskData.orEmpty()
            val newTaskData = TaskData(
                taskId = taskResponse.id,
                documentIds = emptyList(),
                notesIds = emptyList(),
                callIds = emptyList()
            )

            // Update the lead entity directly
            lead.taskData = currentTaskData + newTaskData
            leadRepositoryWrapper.saveWithException(lead)

            LeadTasksResponse(
                leadId = leadId,
                tasks = listOf(taskResponse)
            )
        } catch (e: Exception) {
            throw IllegalStateException("Failed to create task for lead $leadId: ${e.message}", e)
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun patchTaskForLead(leadId: UUID, taskId: UUID, updateTaskRequest: UpdateTaskRequest): LeadTasksResponse {
        // Validate task-lead relationship
        validateTaskLeadRelationship(leadId, taskId)

        // Update task with proper error handling
        val updatedTask = try {
            taskService.patchTaskById(taskId, updateTaskRequest)
        } catch (e: Exception) {
            throw IllegalStateException("Failed to update task $taskId for lead $leadId: ${e.message}", e)
        }

        return LeadTasksResponse(
            leadId = leadId,
            tasks = listOf(updatedTask)
        )
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun deleteTaskForLead(leadId: UUID, taskId: UUID) {
        return try {
            // Validate task-lead relationship
            validateTaskLeadRelationship(leadId, taskId)

            // Get lead for updating task data
            val lead = leadRepositoryWrapper.findByIdWithException(leadId)

            // Remove task from lead's task data
            val currentTaskData = lead.taskData.orEmpty()
            val updatedTaskData = currentTaskData.filter { it.taskId != taskId }

            // Update the lead entity directly
            lead.taskData = updatedTaskData
            leadRepositoryWrapper.saveWithException(lead)

            // Delete the task with proper error handling
            try {
                taskService.deleteTaskById(taskId)
            } catch (e: Exception) {
                throw IllegalStateException("Failed to delete task $taskId for lead $leadId: ${e.message}", e)
            }
        } catch (e: Exception) {
            throw IllegalStateException("Failed to delete task $taskId for lead $leadId: ${e.message}", e)
        }
    }

    /**
     * Validates that a task belongs to a specific lead
     * @param leadId The lead ID to check
     * @param taskId The task ID to validate
     * @throws IllegalArgumentException if the task doesn't belong to the lead
     */
    private fun validateTaskLeadRelationship(leadId: UUID, taskId: UUID) {
        val lead = leadRepositoryWrapper.findByIdWithException(leadId)
        val taskExists = lead.taskData?.any { it.taskId == taskId } ?: false
        require(taskExists) { "Task with ID $taskId does not belong to lead $leadId" }
    }

    /**
     * Validates that all tasks in a list belong to a specific lead
     * @param leadId The lead ID to check
     * @param taskIds The list of task IDs to validate
     * @throws IllegalArgumentException if any task doesn't belong to the lead
     */
    private fun validateTaskLeadRelationships(leadId: UUID, taskIds: List<UUID>) {
        val lead = leadRepositoryWrapper.findByIdWithException(leadId)
        val leadTaskIds = lead.taskData?.map { it.taskId }.orEmpty()
        val invalidTaskIds = taskIds.filter { it !in leadTaskIds }
        require(invalidTaskIds.isEmpty()) {
            "Tasks with IDs $invalidTaskIds do not belong to lead $leadId"
        }
    }
}

