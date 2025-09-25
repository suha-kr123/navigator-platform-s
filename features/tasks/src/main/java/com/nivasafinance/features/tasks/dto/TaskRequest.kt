package com.nivasafinance.features.tasks.dto

import java.util.UUID

data class TaskRequest(
    val taskDefinitionKey: String,
    val description: String?,
    val assignedTo: String?,
    val status: String,
    val outcome: String
)
