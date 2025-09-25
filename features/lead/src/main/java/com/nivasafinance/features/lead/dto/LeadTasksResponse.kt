package com.nivasafinance.features.lead.dto

import java.time.LocalDateTime
import java.util.UUID

data class LeadTasksResponse(
        val leadId: UUID,
        val taskId: UUID,
        val taskDefinitionKey: String,
        val taskName: String,
        val taskType: String,
        val taskDescription: String?,
        val taskAssignedTo: String?,
        val taskStatus: String,
        val taskOutcome: String,
        val taskDueAt: LocalDateTime?,
        val taskCompletedAt: LocalDateTime?,
        val taskRescheduledAt: LocalDateTime?,
        val taskCreatedAt: LocalDateTime,
        val taskCreatedBy: String?,
        val taskUpdatedAt: LocalDateTime,
        val taskUpdatedBy: String?,
)
