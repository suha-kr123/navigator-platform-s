package com.nivasafinance.features.tasks.service

import com.nivasafinance.common.base.model.PaginatedResponse
import com.nivasafinance.common.base.model.PaginationInfo
import com.nivasafinance.common.base.model.PaginationRequest
import com.nivasafinance.features.taskdefinitions.repository.TaskDefinitionRepositoryWrapper
import com.nivasafinance.features.taskhistory.service.TaskHistoryService
import com.nivasafinance.features.tasks.dto.TaskRequest
import com.nivasafinance.features.tasks.dto.TaskResponse
import com.nivasafinance.features.tasks.dto.UpdateTaskRequest
import com.nivasafinance.features.tasks.entity.Task
import com.nivasafinance.features.tasks.enum.TaskStatus
import com.nivasafinance.features.tasks.repository.TaskRepositoryWrapper
import org.springframework.context.MessageSource
import org.springframework.data.domain.PageRequest
import java.time.LocalDateTime
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class TaskServiceImpl(
    private val taskRepositoryWrapper: TaskRepositoryWrapper,
    private val taskDefinitionRepositoryWrapper: TaskDefinitionRepositoryWrapper,
    private val taskOutcomeValidationService: TaskOutcomeValidationService,
    private val taskHistoryService: TaskHistoryService,
    private val messageSource: MessageSource
) : TaskService {

    override fun createTask(taskRequest: TaskRequest): TaskResponse {
        // Fetch task definition to get the task name
        val taskDefinition = taskDefinitionRepositoryWrapper.findByKeyWithException(taskRequest.taskDefinitionKey)

        val task = Task().apply {
            taskDefinitionKey = taskRequest.taskDefinitionKey
            name = taskDefinition.name
            description = taskRequest.description
            assignedTo = taskRequest.assignedTo
            dueAt = taskRequest.dueAt
        }

        val savedTask = taskRepositoryWrapper.saveWithException(task)

        return toTaskResponse(savedTask)
    }

    override fun updateTaskById(taskId: UUID, taskRequest: UpdateTaskRequest): TaskResponse {
        val task = taskRepositoryWrapper.findByIdWithException(taskId)
        val changedBy = "system" // TODO: Get from security context

        // Track description changes
        taskRequest.description?.let { newDescription ->
            task.description = newDescription
        }

        // Track assignment changes
        taskRequest.assignedTo?.let { newAssignee ->
            task.assignedTo = newAssignee
        }

        // Check if due date is changing
        val isDueDateChanging = taskRequest.dueAt?.let { newDueAt -> task.dueAt != newDueAt } ?: false

        // Track due date changes
        taskRequest.dueAt?.let { newDueAt ->
            task.dueAt = newDueAt
        }

        // Track status changes
        taskRequest.status?.let { newStatus ->
            task.status = newStatus
        }

        // When due date changes, always set status to TODO (this overrides any explicit status)
        if (isDueDateChanging && task.status != TaskStatus.TODO) {
            task.status = TaskStatus.TODO
        }

        // Track completion date changes (if explicitly provided)
        taskRequest.completedAt?.let { newCompletedAt ->
            if (task.completedAt != newCompletedAt) {
                task.completedAt = newCompletedAt
                task.completedBy = changedBy
            }
        }

        // Track outcome changes
        taskRequest.outcome?.let { newOutcome ->
            // Validate outcome against current status and task definition
            taskOutcomeValidationService.validateOutcomeOrThrow(
                task.taskDefinitionKey,
                task.status,
                newOutcome
            )
            task.outcome = newOutcome
        }

        val updatedTask = taskRepositoryWrapper.saveWithException(task)
        return toTaskResponse(updatedTask)
    }

    override fun patchTaskById(taskId: UUID, taskRequest: UpdateTaskRequest): TaskResponse {
        return updateTaskById(taskId, taskRequest)
    }

    override fun getTaskById(taskId: UUID): TaskResponse {
        val task = taskRepositoryWrapper.findByIdWithException(taskId)
        return toTaskResponse(task)
    }

    override fun getAllTasks(paginationRequest: PaginationRequest): PaginatedResponse<TaskResponse> {
        val pageable = PageRequest.of(
            paginationRequest.offset / paginationRequest.limit,
            paginationRequest.limit
        )

        val taskPage = taskRepositoryWrapper.findAllWithException(pageable)
        val taskResponses = taskPage.content.map { toTaskResponse(it) }

        val totalPages =
            if (taskPage.totalElements == 0L) 0 else ((taskPage.totalElements - 1) / paginationRequest.limit + 1).toInt()
        val currentPage = paginationRequest.offset / paginationRequest.limit

        return PaginatedResponse(
            content = taskResponses,
            pagination = PaginationInfo(
                offset = paginationRequest.offset,
                limit = paginationRequest.limit,
                totalElements = taskPage.totalElements,
                totalPages = totalPages,
                currentPage = currentPage,
                hasNext = currentPage < totalPages - 1,
                hasPrevious = currentPage > 0
            )
        )
    }

    override fun deleteTaskById(taskId: UUID) {
        taskRepositoryWrapper.deleteByIdWithException(taskId)
    }

    private fun toTaskResponse(task: Task): TaskResponse {
        val taskDefinition = taskDefinitionRepositoryWrapper.findByKeyWithException(task.taskDefinitionKey)
        val taskType = taskDefinition.type

        return TaskResponse(
            id = task.id!!,
            taskDefinitionKey = task.taskDefinitionKey,
            name = task.name,
            taskType = taskType.value,
            description = task.description,
            assignedTo = task.assignedTo,
            status = task.status.value,
            outcome = task.outcome,
            dueAt = task.dueAt,
            completedAt = task.completedAt,
            completedBy = task.completedBy,
            createdAt = task.createdAt!!,
            createdBy = task.createdBy,
            updatedAt = task.updatedAt!!,
            updatedBy = task.updatedBy
        )
    }
}
