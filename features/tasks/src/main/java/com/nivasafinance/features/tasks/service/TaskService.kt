package com.nivasafinance.features.tasks.service

import base.model.PaginatedResponse
import base.model.PaginationRequest
import com.nivasafinance.features.tasks.dto.TaskRequest
import com.nivasafinance.features.tasks.dto.TaskResponse
import com.nivasafinance.features.tasks.dto.UpdateTaskRequest
import java.util.UUID

interface TaskService {

    fun createTask(taskRequest: TaskRequest): TaskResponse

    fun updateTaskById(taskId: UUID, taskRequest: UpdateTaskRequest): TaskResponse

    fun patchTaskById(taskId: UUID, taskRequest: UpdateTaskRequest): TaskResponse

    fun getTaskById(taskId: UUID): TaskResponse

    fun getAllTasks(paginationRequest: PaginationRequest): PaginatedResponse<TaskResponse>

    fun deleteTaskById(taskId: UUID)
}
