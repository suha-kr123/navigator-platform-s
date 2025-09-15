package com.nivasafinance.features.stages.dto

import com.nivasafinance.features.stages.enum.EntityType
import com.nivasafinance.features.stages.enum.Status
import com.nivasafinance.features.tasks.dto.TaskRequest
import java.util.UUID

data class StageRequest(
        val stageDefinitionKey: String,
        val entityType: EntityType,
        val entityId: UUID,
        val outcome: String,
        val status: Status,
        val assignedTo: String?,
        val tasks: List<TaskRequest>
)
