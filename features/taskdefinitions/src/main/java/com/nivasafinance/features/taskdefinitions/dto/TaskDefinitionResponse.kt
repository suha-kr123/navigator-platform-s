package com.nivasafinance.features.taskdefinitions.dto

import com.nivasafinance.features.taskdefinitions.enum.AssignmentStrategy
import com.nivasafinance.features.taskdefinitions.enum.TaskPriority
import java.util.UUID

data class TaskDefinitionResponse(
    val id: UUID,
    val name: String,
    val identifier: String,
    val key: String,
    val description: String? = null,
    val actionsGroup: String,
    val conditionOnAction: String? = null,
    val tatHours: String? = null,
    val assignmentStrategy: AssignmentStrategy,
    val priority: TaskPriority,
    val possibleStatuses: String? = null
)
