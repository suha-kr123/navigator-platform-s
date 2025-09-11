package com.nivasafinance.features.taskdefinitions.dto

import com.nivasafinance.features.taskdefinitions.enum.AssignmentStrategy
import com.nivasafinance.features.taskdefinitions.enum.TaskPriority
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class TaskDefinitionCreateRequest(
    @field:NotBlank(message = "Name is required")
    val name: String,

    @field:NotBlank(message = "Identifier is required")
    val identifier: String,

    @field:NotBlank(message = "Key is required")
    val key: String,

    val description: String? = null,

    @field:NotBlank(message = "Actions group is required")
    val actionsGroup: String,

    val conditionOnAction: String? = null,

    val tatHours: String? = null,

    @field:NotNull(message = "Assignment strategy is required")
    val assignmentStrategy: AssignmentStrategy,

    @field:NotNull(message = "Priority is required")
    val priority: TaskPriority,

    val possibleStatuses: String? = null
)
