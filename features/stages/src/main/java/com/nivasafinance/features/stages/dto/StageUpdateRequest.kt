package com.nivasafinance.features.stages.dto

data class StageUpdateRequest(
    val outcome: String,
    val assignedTo: String?
)