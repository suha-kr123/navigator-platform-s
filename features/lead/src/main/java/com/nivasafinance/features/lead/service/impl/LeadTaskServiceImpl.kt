package com.nivasafinance.features.lead.service.impl

import base.model.PaginatedResponse
import base.model.PaginationInfo
import base.model.PaginationRequest
import com.nivasafinance.features.lead.dto.LeadTasksResponse
import com.nivasafinance.features.lead.entity.TaskData
import com.nivasafinance.features.lead.exception.LeadExceptionFactory
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper
import com.nivasafinance.features.lead.service.LeadTaskService
import com.nivasafinance.features.tasks.dto.TaskRequest
import com.nivasafinance.features.tasks.dto.TaskResponse
import com.nivasafinance.features.tasks.dto.UpdateTaskRequest
import com.nivasafinance.features.tasks.service.TaskService
import org.slf4j.LoggerFactory
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class LeadTaskServiceImpl(
    private val leadRepositoryWrapper: LeadRepositoryWrapper,
    private val taskService: TaskService,
    private val messageSource: MessageSource
) : LeadTaskService {

    private val logger = LoggerFactory.getLogger(LeadTaskServiceImpl::class.java)

    private fun TaskResponse.toLeadTasksResponse(leadId: UUID): LeadTasksResponse {
        return LeadTasksResponse(
            leadId = leadId,
            taskId = this.id,
            taskDefinitionKey = this.taskDefinitionKey,
            taskName = this.name,
            taskType = this.taskType,
            taskDescription = this.description,
            taskAssignedTo = this.assignedTo,
            taskStatus = this.status,
            taskOutcome = this.outcome,
            taskDueAt = this.dueAt,
            taskCompletedAt = this.completedAt,
            taskRescheduledAt = this.rescheduledAt,
            taskCreatedAt = this.createdAt,
            taskCreatedBy = this.createdBy,
            taskUpdatedAt = this.updatedAt,
            taskUpdatedBy = this.updatedBy
        )
    }

    override fun getAllTasks(paginationRequest: PaginationRequest): PaginatedResponse<List<LeadTasksResponse>> {
        return try {
            val allTasks = taskService.getAllTasks(paginationRequest)

            val pageable = org.springframework.data.domain.PageRequest.of(0, 10_000)
            val allLeadsPage = leadRepositoryWrapper.findAllWithException(pageable)
            val allLeads = allLeadsPage.content

            val taskToLeadMap = mutableMapOf<UUID, UUID>()
            allLeads.forEach { lead ->
                lead.taskData?.forEach { taskData ->
                    taskToLeadMap[taskData.taskId] = lead.id!!
                }
            }

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
                    task.toLeadTasksResponse(leadId)
                }

            val filteredPagination = PaginationInfo(
                offset = paginationRequest.offset,
                limit = paginationRequest.limit,
                totalElements = leadTasksResponses.size.toLong(),
                totalPages = if (leadTasksResponses.isEmpty()) {
                    0
                } else {
                    ((leadTasksResponses.size - 1) / paginationRequest.limit + 1)
                },
                currentPage = paginationRequest.offset / paginationRequest.limit,
                hasNext = (paginationRequest.offset + paginationRequest.limit) <
                    leadTasksResponses.size,
                hasPrevious = paginationRequest.offset > 0
            )

            PaginatedResponse(
                content = listOf(leadTasksResponses),
                pagination = filteredPagination
            )
        } catch (e: Exception) {
            throw LeadExceptionFactory.taskRetrievalFailed(messageSource)
        }
    }

    override fun getLeadTasks(leadId: UUID, paginationRequest: PaginationRequest): PaginatedResponse<List<LeadTasksResponse>> {
        return try {
            val lead = leadRepositoryWrapper.findByIdWithException(leadId)

            val taskData = lead.taskData ?: emptyList()

            val leadTasksResponses = taskData.map { taskDataItem ->
                val task = try {
                    taskService.getTaskById(taskDataItem.taskId)
                } catch (e: Exception) {
                    throw LeadExceptionFactory.taskRetrievalFailedForTask(taskDataItem.taskId, leadId, messageSource)
                }
                task.toLeadTasksResponse(leadId)
            }

            val totalElements = leadTasksResponses.size.toLong()
            val totalPages = if (totalElements == 0L) 0 else ((totalElements - 1) / paginationRequest.limit + 1).toInt()
            val currentPage = paginationRequest.offset / paginationRequest.limit

            val paginatedContent = leadTasksResponses
                .drop(paginationRequest.offset)
                .take(paginationRequest.limit)

            PaginatedResponse(
                content = listOf(paginatedContent),
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
            throw LeadExceptionFactory.taskRetrievalFailedForLead(leadId, messageSource)
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun createTaskForLead(leadId: UUID, createTaskRequest: TaskRequest): LeadTasksResponse {
        return try {
            val lead = leadRepositoryWrapper.findByIdWithException(leadId)

            val taskResponse = try {
                taskService.createTask(createTaskRequest)
            } catch (e: Exception) {
                throw LeadExceptionFactory.taskCreationFailedForLead(leadId, messageSource)
            }

            val currentTaskData = lead.taskData.orEmpty()
            val newTaskData = TaskData(
                taskId = taskResponse.id,
            )

            lead.taskData = currentTaskData + newTaskData
            leadRepositoryWrapper.saveWithException(lead)

            taskResponse.toLeadTasksResponse(leadId)
        } catch (e: Exception) {
            throw LeadExceptionFactory.taskCreationFailedForLead(leadId, messageSource)
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun patchTaskForLead(leadId: UUID, taskId: UUID, updateTaskRequest: UpdateTaskRequest): LeadTasksResponse {
        validateTaskLeadRelationship(leadId, taskId)

        val updatedTask = try {
            taskService.patchTaskById(taskId, updateTaskRequest)
        } catch (e: Exception) {
            throw LeadExceptionFactory.taskUpdateFailed(taskId, leadId, messageSource)
        }

        return updatedTask.toLeadTasksResponse(leadId)
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun deleteTaskForLead(leadId: UUID, taskId: UUID) {
        return try {
            validateTaskLeadRelationship(leadId, taskId)

            val lead = leadRepositoryWrapper.findByIdWithException(leadId)

            val currentTaskData = lead.taskData.orEmpty()
            val updatedTaskData = currentTaskData.filter { it.taskId != taskId }

            lead.taskData = updatedTaskData
            leadRepositoryWrapper.saveWithException(lead)

            try {
                taskService.deleteTaskById(taskId)
            } catch (e: Exception) {
                throw LeadExceptionFactory.taskDeletionFailed(taskId, leadId, messageSource)
            }
        } catch (e: Exception) {
            throw LeadExceptionFactory.taskDeletionFailed(taskId, leadId, messageSource)
        }
    }

    override fun getTaskForLead(leadId: UUID, taskId: UUID): LeadTasksResponse {
        return try {
            validateTaskLeadRelationship(leadId, taskId)

            val task = try {
                taskService.getTaskById(taskId)
            } catch (e: Exception) {
                throw LeadExceptionFactory.taskRetrievalFailedForTask(taskId, leadId, messageSource)
            }

            task.toLeadTasksResponse(leadId)
        } catch (e: Exception) {
            throw LeadExceptionFactory.taskRetrievalFailedForTask(taskId, leadId, messageSource)
        }
    }

    /**
     * @param leadId The lead ID to check
     * @param taskId The task ID to validate
     * @throws TaskNotBelongsToLeadException if the task doesn't belong to the lead
     */
    private fun validateTaskLeadRelationship(leadId: UUID, taskId: UUID) {
        val lead = leadRepositoryWrapper.findByIdWithException(leadId)
        val taskExists = lead.taskData?.any { it.taskId == taskId } ?: false
        if (!taskExists) {
            throw LeadExceptionFactory.taskNotBelongsToLead(taskId, leadId, messageSource)
        }
    }

    /**
     * @param leadId The lead ID to check
     * @param taskIds The list of task IDs to validate
     * @throws TasksNotBelongToLeadException if any task doesn't belong to the lead
     */
    private fun validateTaskLeadRelationships(leadId: UUID, taskIds: List<UUID>) {
        val lead = leadRepositoryWrapper.findByIdWithException(leadId)
        val leadTaskIds = lead.taskData?.map { it.taskId }.orEmpty()
        val invalidTaskIds = taskIds.filter { it !in leadTaskIds }
        if (invalidTaskIds.isNotEmpty()) {
            throw LeadExceptionFactory.tasksNotBelongToLead(invalidTaskIds, leadId, messageSource)
        }
    }
}
