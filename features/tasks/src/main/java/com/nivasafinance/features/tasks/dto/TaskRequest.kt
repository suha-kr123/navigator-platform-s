package com.nivasafinance.features.tasks.dto

import java.util.UUID
import java.time.LocalDateTime

data class TaskRequest(
    val taskDefinitionKey: String,
    val description: String?,
    val assignedTo: String?,
    val dueAt: LocalDateTime?,
    val status: String
)
