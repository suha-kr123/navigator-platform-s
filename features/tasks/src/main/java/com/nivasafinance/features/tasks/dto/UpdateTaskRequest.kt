package com.nivasafinance.features.tasks.dto

import java.time.LocalDateTime

data class UpdateTaskRequest(
    val taskData: Map<String, Any>?,
    val assignedTo: String?,
    val dueAt: LocalDateTime?,
    val completedAt: LocalDateTime?,
    val rescheduledAt: LocalDateTime?,
    val status: String,
    val outcome: String
)
