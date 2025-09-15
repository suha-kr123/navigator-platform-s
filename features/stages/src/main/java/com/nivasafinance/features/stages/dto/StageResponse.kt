package com.nivasafinance.features.stages.dto

import com.nivasafinance.features.stages.enum.Outcome
import com.nivasafinance.features.stages.enum.Status
import com.nivasafinance.features.stages.enum.EntityType
// import com.nivasafinance.features.tasks.dto.TaskResponse
import java.time.LocalDateTime
import java.util.*

data class StageResponse(
    val id: UUID,
    val entityType: EntityType,
    val entityId: UUID,
    val stageDefinitionKey: String,
    val outcome: Outcome,
    val status: Status,
    val assignedTo: String?,
    val tasks: List<String>, // Using String instead of TaskResponse for now
    val createdAt: LocalDateTime,
    val createdBy: String?,
    val updatedAt: LocalDateTime,
    val updatedBy: String?,
)
