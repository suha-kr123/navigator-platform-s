package com.nivasafinance.features.tasks.service

import base.model.PaginatedResponse
import base.model.PaginationInfo
import base.model.PaginationRequest
import com.nivasafinance.features.taskdefinitions.repository.TaskDefinitionRepositoryWrapper
import com.nivasafinance.features.tasks.dto.TaskRequest
import com.nivasafinance.features.tasks.dto.TaskResponse
import com.nivasafinance.features.tasks.dto.UpdateTaskRequest
import com.nivasafinance.features.tasks.entity.Task
import com.nivasafinance.features.tasks.repository.TaskRepositoryWrapper
import org.springframework.context.MessageSource
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class TaskServiceImpl(
    private val taskRepositoryWrapper: TaskRepositoryWrapper,
    private val taskDefinitionRepositoryWrapper: TaskDefinitionRepositoryWrapper,
    private val messageSource: MessageSource
) : TaskService {

    override fun createTask(taskRequest: TaskRequest): TaskResponse {
        val task = Task(
            taskDefinitionKey = taskRequest.taskDefinitionKey,
            description = taskRequest.description,
            assignedTo = taskRequest.assignedTo,
            status = taskRequest.status,
            outcome = null,
            dueAt = taskRequest.dueAt,
            completedAt = null,
            rescheduledAt = null
        )

        val savedTask = taskRepositoryWrapper.saveWithException(task)
        return toTaskResponse(savedTask)
    }

    override fun updateTaskById(taskId: UUID, taskRequest: UpdateTaskRequest): TaskResponse {
        val task = taskRepositoryWrapper.findByIdWithException(taskId)

        taskRequest.description?.let { task.description = it }
        taskRequest.assignedTo?.let { task.assignedTo = it }
        taskRequest.status?.let { task.status = it }
        taskRequest.outcome?.let { task.outcome = it }
        taskRequest.dueAt?.let { task.dueAt = it }
        taskRequest.completedAt?.let { task.completedAt = it }
        taskRequest.rescheduledAt?.let { task.rescheduledAt = it }

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

    override fun deleteTaskById(taskId: UUID) {
        taskRepositoryWrapper.deleteByIdWithException(taskId)
    }


    private fun toTaskResponse(task: Task): TaskResponse {
        val taskDefinition = taskDefinitionRepositoryWrapper.findByKeyWithException(task.taskDefinitionKey)

        val taskType = taskDefinition.type
        val taskName = taskDefinition.name

        return TaskResponse(
            id = task.id!!,
            taskDefinitionKey = task.taskDefinitionKey,
            name = taskName,
            taskType = taskType.value,
            description = task.description,
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

