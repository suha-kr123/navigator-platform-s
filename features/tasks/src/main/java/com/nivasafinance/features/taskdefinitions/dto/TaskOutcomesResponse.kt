package com.nivasafinance.features.taskdefinitions.dto

import com.nivasafinance.features.tasks.dto.TaskOutcome

data class TaskOutcomesResponse(
    val taskDefinitionKey: String,
    val taskDefinitionName: String,
    val outcomes: List<TaskOutcome>,
    val statusOutcomeMapping: Map<String, List<TaskOutcome>>? = null
)
