package com.nivasafinance.features.lead.dto

import com.nivasafinance.features.tasks.enum.TaskOutcome
import com.nivasafinance.features.tasks.enum.TaskStatus
import java.time.LocalDateTime
import java.util.*

data class TaskResponse(
    val id: UUID,
    val taskKey: String,
    val entityId: UUID,
    val entityType: String,
    val assignedTo: String?,
    val status: TaskStatus,
    val outcome: TaskOutcome,
    val taskData: String?,
    val dueAt: LocalDateTime?,
    val completedAt: LocalDateTime?,
    val rescheduledAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
