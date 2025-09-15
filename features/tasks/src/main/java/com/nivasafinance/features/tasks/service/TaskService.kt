package com.nivasafinance.features.tasks.service

import com.nivasafinance.features.tasks.dto.TaskRequest
import com.nivasafinance.features.tasks.dto.TaskResponse
import com.nivasafinance.features.tasks.dto.UpdateTaskRequest
import com.nivasafinance.features.tasks.entity.Task
import java.util.UUID

interface TaskService {

    fun createTask(taskRequest: TaskRequest): TaskResponse

    fun updateTask(taskId: UUID, taskRequest: UpdateTaskRequest): TaskResponse

    fun deleteTask(taskId: UUID)

    fun getTask(taskId: UUID): TaskResponse

    fun getTasks(taskIds: List<UUID>): List<TaskResponse>

    fun getTasksByAssignedTo(assignedTo: String): List<TaskResponse>
}
