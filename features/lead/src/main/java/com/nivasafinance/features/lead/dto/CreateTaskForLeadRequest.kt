package com.nivasafinance.features.lead.dto

import java.util.UUID

data class CreateTaskForLeadRequest(
    val taskDefinitionKey: String,
    val description: String?,
    val assignedTo: String?,
    val status: String = "PENDING",
    val outcome: String = "PENDING"
)
