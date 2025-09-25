package com.nivasafinance.features.lead.dto

import com.nivasafinance.features.tasks.dto.TaskResponse
import java.util.*

data class LeadTasksResponse(
    val leadId: UUID,
    val tasks: List<TaskResponse>
)
