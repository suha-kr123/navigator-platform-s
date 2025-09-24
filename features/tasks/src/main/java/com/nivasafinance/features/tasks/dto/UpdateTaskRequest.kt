package com.nivasafinance.features.tasks.dto

import java.time.LocalDateTime
import java.util.UUID

data class UpdateTaskRequest(
    val description: String?,
    val taskData: Map<String, Any>?,
    val assignedTo: String?,
    val dueAt: LocalDateTime?,
    val completedAt: LocalDateTime?,
    val rescheduledAt: LocalDateTime?,
    val status: String?,
    val outcome: String?,
    val noteIds: List<UUID>?
)
