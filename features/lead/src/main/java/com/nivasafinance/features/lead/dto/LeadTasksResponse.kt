package com.nivasafinance.features.lead.dto

import com.nivasafinance.features.tasks.enum.TaskStatus
import java.time.LocalDateTime
import java.util.UUID

data class LeadTasksResponse(
    val leadId: UUID,
    val taskId: UUID,
    val taskPrimaryContact: String?,
    val taskPrimaryContactPhone: String?,
    val taskDefinitionKey: String,
    val taskName: String,
    val taskType: String,
    val taskDescription: String?,
    val taskAssignedTo: String?,
    val taskStatus: TaskStatus,
    val taskCompletedAt: LocalDateTime?,
    val taskCompletedBy: String?,
    val taskOutcome: String?,
    val taskDueAt: LocalDateTime?,
    val taskCreatedAt: LocalDateTime,
    val taskCreatedBy: String?,
    val taskUpdatedAt: LocalDateTime,
    val taskUpdatedBy: String?,
)
