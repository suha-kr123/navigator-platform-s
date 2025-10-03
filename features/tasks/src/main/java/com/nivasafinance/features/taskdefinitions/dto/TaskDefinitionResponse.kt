package com.nivasafinance.features.taskdefinitions.dto

import java.util.UUID

data class TaskDefinitionResponse(
    val id: UUID,
    val name: String,
    val key: String,
    val type: String,
    val description: String?,
    val possibleOutcomes: List<String>?
)
