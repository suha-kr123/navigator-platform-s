package com.nivasafinance.features.stages.dto

import java.time.LocalDateTime
import java.util.*

data class StageResponse(
    val id: UUID,
    val entityType: String,
    val entityId: UUID,
    val stageDefinitionKey: String,
    val outcome: String,
    val assignedTo: String?,
    val createdAt: LocalDateTime,
    val createdBy: String?,
    val updatedAt: LocalDateTime,
    val updatedBy: String?
)
