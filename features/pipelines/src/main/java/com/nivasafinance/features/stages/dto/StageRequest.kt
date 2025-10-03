package com.nivasafinance.features.stages.dto

data class StageRequest(
    val stageDefinitionKey: String,
    val outcome: String,
    val assignedTo: String?
)
