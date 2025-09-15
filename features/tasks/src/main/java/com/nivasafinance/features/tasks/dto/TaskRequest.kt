package com.nivasafinance.features.tasks.dto

import com.nivasafinance.features.tasks.enum.TaskStatus
import com.nivasafinance.features.tasks.enum.TaskOutcome

data class TaskRequest(
    val taskDefinitionKey: String,
    val taskData: String?,
    val assignedTo: String?,
    val status: TaskStatus,
    val outcome: TaskOutcome
)