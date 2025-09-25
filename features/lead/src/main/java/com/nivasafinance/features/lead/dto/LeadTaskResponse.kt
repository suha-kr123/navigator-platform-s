package com.nivasafinance.features.lead.dto

import com.nivasafinance.features.tasks.dto.TaskResponse
import java.util.UUID

data class LeadTaskResponse(
    val leadId: UUID,
    val task: TaskResponse
)
