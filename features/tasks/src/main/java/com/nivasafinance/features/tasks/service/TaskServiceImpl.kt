package com.nivasafinance.features.tasks.service

import base.model.PaginatedResponse
import base.model.PaginationInfo
import base.model.PaginationRequest
import com.nivasafinance.features.taskdefinitions.repository.TaskDefinitionRepositoryWrapper
import com.nivasafinance.features.tasks.dto.TaskRequest
import com.nivasafinance.features.tasks.dto.TaskResponse
import com.nivasafinance.features.tasks.dto.UpdateTaskRequest
import com.nivasafinance.features.tasks.entity.Task
import com.nivasafinance.features.tasks.enum.EntityType
import com.nivasafinance.features.tasks.exception.TaskExceptionFactory
import com.nivasafinance.features.tasks.exception.TaskOperationException
import com.nivasafinance.features.tasks.repository.TaskRepositoryWrapper
import org.springframework.context.MessageSource
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class TaskServiceImpl(
    private val taskRepositoryWrapper: TaskRepositoryWrapper,
    private val taskDefinitionRepositoryWrapper: TaskDefinitionRepositoryWrapper,
    private val messageSource: MessageSource
) : TaskService {

    override fun createTaskByEntity(entityType: String, entityId: UUID, taskRequest: TaskRequest): TaskResponse {
        validateEntityType(entityType)

        val exists = taskRepositoryWrapper.existsByEntityTypeAndEntityIdAndTaskDefinitionKeyWithException(
            entityType,
            entityId,
            taskRequest.taskDefinitionKey
        )
        if (exists) {
            throw TaskExceptionFactory.taskAlreadyExists(taskRequest.taskDefinitionKey, messageSource)
        }

        val task = Task(
            taskDefinitionKey = taskRequest.taskDefinitionKey,
            description = taskRequest.description,
            entityType = entityType,
            entityId = entityId,
            taskData = taskRequest.taskData,
            assignedTo = taskRequest.assignedTo,
            status = taskRequest.status,
            outcome = taskRequest.outcome,
            dueAt = null,
            completedAt = null,
            rescheduledAt = null,
            noteIds = taskRequest.noteIds
        )

        val savedTask = taskRepositoryWrapper.saveWithException(task)
        return toTaskResponse(savedTask)
    }

    override fun updateTaskByEntity(entityType: String, entityId: UUID, taskId: UUID, taskRequest: UpdateTaskRequest): TaskResponse {
        validateEntityType(entityType)

        val existingTasks = taskRepositoryWrapper.findAllByEntityTypeAndEntityIdWithException(entityType, entityId)
        val existingTask = existingTasks.firstOrNull { it.id == taskId }

        if (existingTask == null) {
            throw TaskExceptionFactory.taskNotFound(taskId, messageSource)
        }

        val updatedTask = existingTask.copy(
            description = taskRequest.description,
            taskData = taskRequest.taskData,
            assignedTo = taskRequest.assignedTo,
            dueAt = taskRequest.dueAt,
            completedAt = taskRequest.completedAt,
            rescheduledAt = taskRequest.rescheduledAt,
            status = taskRequest.status ?: existingTask.status,
            outcome = taskRequest.outcome ?: existingTask.outcome,
            noteIds = taskRequest.noteIds
        )

        val savedTask = taskRepositoryWrapper.saveWithException(updatedTask)
        return toTaskResponse(savedTask)
    }

    override fun updateTaskById(taskId: UUID, taskRequest: UpdateTaskRequest): TaskResponse {
        val existingTask = taskRepositoryWrapper.findByIdWithException(taskId)

        val updatedTask = existingTask.copy(
            description = taskRequest.description ?: existingTask.description,
            taskData = taskRequest.taskData ?: existingTask.taskData,
            assignedTo = taskRequest.assignedTo ?: existingTask.assignedTo,
            dueAt = taskRequest.dueAt ?: existingTask.dueAt,
            completedAt = taskRequest.completedAt ?: existingTask.completedAt,
            rescheduledAt = taskRequest.rescheduledAt ?: existingTask.rescheduledAt,
            status = taskRequest.status ?: existingTask.status,
            outcome = taskRequest.outcome ?: existingTask.outcome,
            noteIds = taskRequest.noteIds ?: existingTask.noteIds
        )

        val savedTask = taskRepositoryWrapper.saveWithException(updatedTask)
        return toTaskResponse(savedTask)
    }

    override fun patchTaskById(taskId: UUID, taskRequest: UpdateTaskRequest): TaskResponse {
        val existingTask = taskRepositoryWrapper.findByIdWithException(taskId)

        // Update only the fields that are provided in the request
        if (taskRequest.description != null) {
            existingTask.description = taskRequest.description
        }
        if (taskRequest.taskData != null) {
            existingTask.taskData = taskRequest.taskData
        }
        if (taskRequest.assignedTo != null) {
            existingTask.assignedTo = taskRequest.assignedTo
        }
        if (taskRequest.dueAt != null) {
            existingTask.dueAt = taskRequest.dueAt
        }
        if (taskRequest.completedAt != null) {
            existingTask.completedAt = taskRequest.completedAt
        }
        if (taskRequest.rescheduledAt != null) {
            existingTask.rescheduledAt = taskRequest.rescheduledAt
        }
        if (taskRequest.status != null) {
            existingTask.status = taskRequest.status
        }
        if (taskRequest.outcome != null) {
            existingTask.outcome = taskRequest.outcome
        }
        if (taskRequest.noteIds != null) {
            existingTask.noteIds = taskRequest.noteIds
        }

        val savedTask = taskRepositoryWrapper.saveWithException(existingTask)
        return toTaskResponse(savedTask)
    }

    override fun getTaskById(taskId: UUID): TaskResponse {
        val task = taskRepositoryWrapper.findByIdWithException(taskId)
        return toTaskResponse(task)
    }

    override fun getTasksByEntity(entityType: String, entityId: UUID): List<TaskResponse> {
        validateEntityType(entityType)

        val tasks = taskRepositoryWrapper.findAllByEntityTypeAndEntityIdWithException(entityType, entityId)
        return tasks.map { toTaskResponse(it) }
    }

    override fun getTasksByEntityTypeAndEntityIds(entityType: String, entityIds: List<UUID>, paginationRequest: PaginationRequest): PaginatedResponse<TaskResponse> {
        validateEntityType(entityType)

        val pageable = PageRequest.of(
            paginationRequest.offset / paginationRequest.limit,
            paginationRequest.limit
        )

        val taskPage = taskRepositoryWrapper.findAllByEntityTypeAndEntityIdInWithException(entityType, entityIds, pageable)
        val taskResponses = taskPage.toList().map { toTaskResponse(it) }

        val totalElements = taskPage.totalElements
        val totalPages = if (totalElements == 0L) 0 else ((totalElements - 1) / paginationRequest.limit + 1).toInt()
        val currentPage = paginationRequest.offset / paginationRequest.limit

        return PaginatedResponse(
            content = taskResponses,
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
    }

    override fun getAllTasksByEntityType(entityType: String, paginationRequest: PaginationRequest): PaginatedResponse<TaskResponse> {
        validateEntityType(entityType)

        val pageable = PageRequest.of(
            paginationRequest.offset / paginationRequest.limit,
            paginationRequest.limit
        )

        val taskPage = taskRepositoryWrapper.findAllByEntityTypeWithException(entityType, pageable)
        val taskResponses = taskPage.content.map { toTaskResponse(it) }

        val totalPages = if (taskPage.totalElements == 0L) 0 else ((taskPage.totalElements - 1) / paginationRequest.limit + 1).toInt()
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

    private fun validateEntityType(entityType: String) {
        try {
            EntityType.valueOf(entityType)
        } catch (e: IllegalArgumentException) {
            throw TaskExceptionFactory.unsupportedEntityType(entityType, messageSource)
        }
    }

    private fun toTaskResponse(task: Task): TaskResponse {
        val taskDefinition = try {
            taskDefinitionRepositoryWrapper.findByKeyWithException(task.taskDefinitionKey)
        } catch (e: Exception) {
            null
        }
        val taskType = taskDefinition?.type ?: "UNKNOWN"
        
        // Skip validation for UNKNOWN task type to avoid errors
        if (taskType != "UNKNOWN") {
            validateTaskType(taskType)
        }

        return TaskResponse(
            id = task.id!!,
            taskDefinitionKey = task.taskDefinitionKey,
            name = taskDefinition?.name ?: "",
            taskType = taskType,
            description = task.description,
            entityType = task.entityType,
            entityId = task.entityId,
            taskData = task.taskData,
            assignedTo = task.assignedTo,
            status = task.status,
            outcome = task.outcome,
            dueAt = task.dueAt,
            completedAt = task.completedAt,
            rescheduledAt = task.rescheduledAt,
            noteIds = task.noteIds,
            createdAt = task.createdAt!!,
            createdBy = task.createdBy,
            updatedAt = task.updatedAt!!,
            updatedBy = task.updatedBy
        )
    }

    private fun validateTaskType(taskType: String) {
        if (taskType !in listOf("HOUSE_VISIT", "DOCUMENT_COLLECTION", "UNKNOWN")) {
            throw TaskExceptionFactory.unsupportedTaskType(taskType, messageSource)
        }
    }
}
