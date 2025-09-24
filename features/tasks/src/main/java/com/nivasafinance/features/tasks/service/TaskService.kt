package com.nivasafinance.features.tasks.service

import base.model.PaginatedResponse
import base.model.PaginationRequest
import com.nivasafinance.features.tasks.dto.TaskRequest
import com.nivasafinance.features.tasks.dto.TaskResponse
import com.nivasafinance.features.tasks.dto.UpdateTaskRequest
import java.util.UUID

interface TaskService {

    fun createTaskByEntity(entityType: String, entityId: UUID, taskRequest: TaskRequest): TaskResponse

    fun updateTaskByEntity(entityType: String, entityId: UUID, taskId: UUID, taskRequest: UpdateTaskRequest): TaskResponse

    fun updateTaskById(taskId: UUID, taskRequest: UpdateTaskRequest): TaskResponse
    
    fun patchTaskById(taskId: UUID, taskRequest: UpdateTaskRequest): TaskResponse

    fun getTaskById(taskId: UUID): TaskResponse

    fun getTasksByEntity(entityType: String, entityId: UUID): List<TaskResponse>

    fun getTasksByEntityTypeAndEntityIds(entityType: String, entityIds: List<UUID>, paginationRequest: PaginationRequest): PaginatedResponse<TaskResponse>

    fun getAllTasksByEntityType(entityType: String, paginationRequest: PaginationRequest): PaginatedResponse<TaskResponse>
}
