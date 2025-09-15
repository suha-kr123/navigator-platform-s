package com.nivasafinance.features.lead.dto

import java.util.*

data class CreateTaskResponse(
    val id: UUID,
    val taskKey: String,
    val entityId: UUID,
    val entityType: String,
    val assignedTo: String?,
    val status: String,
    val outcome: String,
    val createdAt: String
)
