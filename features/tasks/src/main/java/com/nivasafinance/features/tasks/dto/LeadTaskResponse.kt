package com.nivasafinance.features.tasks.dto

import com.nivasafinance.features.tasks.entity.LeadTask
import com.nivasafinance.features.tasks.enum.TaskStatus
import java.time.LocalDateTime
import java.util.UUID

data class LeadTaskResponse(
    val id: UUID,
    val taskKey: String,
    val leadId: UUID,
    val stage: String,
    val taskData: Map<String, Any>? = null,
    val assignedTo: String? = null,
    val status: TaskStatus,
    val dueAt: LocalDateTime? = null,
    val completedAt: LocalDateTime? = null,
    val rescheduledAt: LocalDateTime? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun fromEntity(leadTask: LeadTask): LeadTaskResponse {
            return LeadTaskResponse(
                id = leadTask.id!!,
                taskKey = leadTask.taskKey,
                leadId = leadTask.leadId,
                stage = leadTask.stage,
                taskData = leadTask.taskData,
                assignedTo = leadTask.assignedTo,
                status = leadTask.status,
                dueAt = leadTask.dueAt,
                completedAt = leadTask.completedAt,
                rescheduledAt = leadTask.rescheduledAt,
                createdAt = leadTask.createdAt ?: LocalDateTime.now(),
                updatedAt = leadTask.updatedAt ?: LocalDateTime.now()
            )
        }
    }
}
