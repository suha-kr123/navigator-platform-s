package com.nivasafinance.features.lead.dto

import com.nivasafinance.features.tasks.enum.TaskOutcome
import com.nivasafinance.features.tasks.enum.TaskStatus
import java.time.LocalDateTime

data class UpdateTaskRequest(
    val status: TaskStatus? = null,
    val outcome: TaskOutcome? = null,
    val assignedTo: String? = null,
    val taskData: String? = null,
    val dueAt: LocalDateTime? = null,
    val completedAt: LocalDateTime? = null,
    val rescheduledAt: LocalDateTime? = null
)
