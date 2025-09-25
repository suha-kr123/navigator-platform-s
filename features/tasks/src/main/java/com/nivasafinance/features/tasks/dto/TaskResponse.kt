package com.nivasafinance.features.tasks.dto

import java.time.LocalDateTime
import java.util.*

data class TaskResponse(
    val id: UUID,
    val taskDefinitionKey: String,
    val name: String,
    val taskType: String,
    val description: String?,
    val assignedTo: String?,
    val status: String,
    val outcome: String,
    val dueAt: LocalDateTime?,
    val completedAt: LocalDateTime?,
    val rescheduledAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val createdBy: String?,
    val updatedAt: LocalDateTime,
    val updatedBy: String?,
)
