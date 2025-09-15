package com.nivasafinance.features.tasks.dto

import com.nivasafinance.features.tasks.enum.TaskStatus
import java.time.LocalDateTime

data class UpdateTaskRequest(
    val taskData: String?,
    val assignedTo: String?,
    val dueAt: LocalDateTime?,
    val completedAt: LocalDateTime?,
    val rescheduledAt: LocalDateTime?,
    val status: TaskStatus,
    val outcome: String
)
