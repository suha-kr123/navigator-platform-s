package com.nivasafinance.features.tasks.service

import base.model.PaginatedResponse
import base.model.PaginationInfo
import base.model.PaginationRequest
import com.nivasafinance.features.tasks.dto.TaskRequest
import com.nivasafinance.features.tasks.dto.TaskResponse
import com.nivasafinance.features.tasks.dto.UpdateTaskRequest
import com.nivasafinance.features.tasks.entity.Task
import com.nivasafinance.features.tasks.exception.TaskExceptionFactory
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
    private val messageSource: MessageSource
) : TaskService {

    override fun createTaskByEntity(entityType: String, entityId: UUID, taskRequest: TaskRequest): TaskResponse {
        validateEntityType(entityType)
        
        val exists = taskRepositoryWrapper.existsByEntityTypeAndEntityIdAndTaskDefinitionKeyWithException(
            entityType, entityId, taskRequest.taskDefinitionKey
        )
        if (exists) {
            throw TaskExceptionFactory.taskAlreadyExists(taskRequest.taskDefinitionKey, messageSource)
        }
        
        val task = Task(
            taskDefinitionKey = taskRequest.taskDefinitionKey,
            entityType = entityType,
            entityId = entityId,
            taskData = taskRequest.taskData,
            assignedTo = taskRequest.assignedTo,
            status = taskRequest.status,
            outcome = taskRequest.outcome,
            dueAt = null,
            completedAt = null,
            rescheduledAt = null
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
            taskData = taskRequest.taskData,
            assignedTo = taskRequest.assignedTo,
            dueAt = taskRequest.dueAt,
            completedAt = taskRequest.completedAt,
            rescheduledAt = taskRequest.rescheduledAt,
            status = taskRequest.status,
            outcome = taskRequest.outcome
        )
        
        val savedTask = taskRepositoryWrapper.saveWithException(updatedTask)
        return toTaskResponse(savedTask)
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

    private fun validateEntityType(entityType: String) {
        if (entityType != "STAGE") {
            throw TaskExceptionFactory.unsupportedEntityType(entityType, messageSource)
        }
    }

    private fun toTaskResponse(task: Task): TaskResponse {
        return TaskResponse(
            id = task.id!!,
            taskDefinitionKey = task.taskDefinitionKey,
            taskData = task.taskData,
            assignedTo = task.assignedTo,
            status = task.status,
            outcome = task.outcome,
            dueAt = task.dueAt,
            completedAt = task.completedAt,
            rescheduledAt = task.rescheduledAt,
            createdAt = task.createdAt!!,
            createdBy = task.createdBy,
            updatedAt = task.updatedAt!!,
            updatedBy = task.updatedBy
        )
    }
}