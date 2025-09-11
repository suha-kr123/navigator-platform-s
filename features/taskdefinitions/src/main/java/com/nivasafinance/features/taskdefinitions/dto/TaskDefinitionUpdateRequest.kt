package com.nivasafinance.features.taskdefinitions.dto

import com.nivasafinance.features.taskdefinitions.enum.AssignmentStrategy
import com.nivasafinance.features.taskdefinitions.enum.TaskPriority

data class TaskDefinitionUpdateRequest(
    val name: String? = null,
    val identifier: String? = null,
    val key: String? = null,
    val description: String? = null,
    val actionsGroup: String? = null,
    val conditionOnAction: String? = null,
    val tatHours: String? = null,
    val assignmentStrategy: AssignmentStrategy? = null,
    val priority: TaskPriority? = null,
    val possibleStatuses: String? = null
)
