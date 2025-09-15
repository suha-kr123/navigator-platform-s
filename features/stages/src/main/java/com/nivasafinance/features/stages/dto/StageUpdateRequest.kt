package com.nivasafinance.features.stages.dto

import com.nivasafinance.features.stages.enum.Status

data class StageUpdateRequest(
    val outcome: String,
    val status: Status,
    val assignedTo: String?
)