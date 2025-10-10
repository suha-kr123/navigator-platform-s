package com.nivasafinance.features.taskhistory.dto

import com.nivasafinance.features.taskhistory.enum.TaskEventType
import java.time.LocalDateTime
import java.util.UUID

/**
 * DTO for task history response
 */
data class TaskHistoryResponse(
    val id: UUID,
    val taskId: UUID,
    val eventType: TaskEventType,
    val oldValue: String?,
    val newValue: String?,
    val changedBy: String,
    val changedAt: LocalDateTime
)
