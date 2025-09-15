package com.nivasafinance.features.stages.dto

import com.nivasafinance.features.stages.enum.Outcome
import com.nivasafinance.features.stages.enum.Status

data class StageUpdateRequest(
    val outcome: Outcome,
    val status: Status,
    val assignedTo: String?
)