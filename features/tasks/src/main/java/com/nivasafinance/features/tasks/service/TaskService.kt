package com.nivasafinance.features.tasks.service

import com.nivasafinance.features.tasks.entity.Task
import java.util.UUID

interface TaskService {

    fun createTask(task: Task): Task

    fun updateTask(task: Task): Task

    fun deleteTask(taskId: UUID)

    fun getTask(taskId: UUID): Task
}