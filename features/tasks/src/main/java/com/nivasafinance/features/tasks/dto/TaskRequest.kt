package com.nivasafinance.features.tasks.dto

data class TaskRequest(
    val taskDefinitionKey: String,
    val taskData: Map<String, Any>?,
    val assignedTo: String?,
    val status: String,
    val outcome: String
)