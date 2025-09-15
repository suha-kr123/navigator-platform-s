package com.nivasafinance.features.tasks.dto

import com.nivasafinance.features.tasks.enum.TaskStatus
import java.time.LocalDateTime
import java.util.*

data class TaskResponse(
    val id: UUID,
    val taskDefinitionKey: String,
    val taskData: String?,
    val assignedTo: String?,
    val status: TaskStatus,
    val outcome: String,
    val dueAt: LocalDateTime?,
    val completedAt: LocalDateTime?,
    val rescheduledAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val createdBy: String?,
    val updatedAt: LocalDateTime,
    val updatedBy: String?,
)
