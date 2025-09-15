package com.nivasafinance.features.tasks.service

import com.nivasafinance.features.tasks.dto.TaskRequest
import com.nivasafinance.features.tasks.dto.TaskResponse
import com.nivasafinance.features.tasks.dto.UpdateTaskRequest
import com.nivasafinance.features.tasks.entity.Task
import com.nivasafinance.features.tasks.exception.TaskExceptionFactory
import com.nivasafinance.features.tasks.repository.TaskRepositoryWrapper
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional
class TaskServiceImpl(
    private val taskRepositoryWrapper: TaskRepositoryWrapper,
    private val messageSource: MessageSource
) : TaskService {

    override fun createTask(taskRequest: TaskRequest): TaskResponse {
        TaskExceptionFactory.validateTaskForCreation(
            taskRequest.taskDefinitionKey,
            taskRequest.taskDefinitionKey,
            taskRequest.status,
            taskRequest.outcome,
            null,
            taskRequest.assignedTo,
            messageSource
        )
        
        val task = Task(
            taskDefinitionKey = taskRequest.taskDefinitionKey,
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

    override fun updateTask(taskId: UUID, taskRequest: UpdateTaskRequest): TaskResponse {
        val existingTask = getTaskById(taskId)
        
        TaskExceptionFactory.validateTaskForUpdate(
            taskId,
            existingTask.taskDefinitionKey,
            existingTask.taskDefinitionKey,
            taskRequest.status,
            taskRequest.outcome,
            existingTask.dueAt,
            taskRequest.assignedTo,
            messageSource
        )
        
        val updatedTask = existingTask.copy(
            taskData = taskRequest.taskData,
            assignedTo = taskRequest.assignedTo,
            status = taskRequest.status,
            outcome = taskRequest.outcome
        )
        
        val savedTask = taskRepositoryWrapper.saveWithException(updatedTask)
        return toTaskResponse(savedTask)
    }

    override fun deleteTask(taskId: UUID) {
        getTaskById(taskId)
        taskRepositoryWrapper.deleteByIdWithException(taskId)
    }

    override fun getTask(taskId: UUID): TaskResponse {
        val task = getTaskById(taskId)
        return toTaskResponse(task)
    }

    private fun getTaskById(taskId: UUID): Task {
        return taskRepositoryWrapper.findByIdWithException(taskId)
    }

    override fun getTasks(taskIds: List<UUID>): List<TaskResponse> {
        val tasks = taskRepositoryWrapper.findAllWithException(taskIds)
        return tasks.map { toTaskResponse(it) }
    }

    override fun getTasksByAssignedTo(assignedTo: String): List<TaskResponse> {
        val tasks = taskRepositoryWrapper.findAllByAssignedToWithException(assignedTo)
        return tasks.map { toTaskResponse(it) }
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
