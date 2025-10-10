package com.nivasafinance.features.taskhistory.service

import com.nivasafinance.common.base.model.PaginatedResponse
import com.nivasafinance.common.base.model.PaginationRequest
import com.nivasafinance.features.taskhistory.dto.TaskHistoryResponse
import com.nivasafinance.features.tasks.enum.TaskStatus
import java.time.LocalDateTime
import java.util.UUID

interface TaskHistoryService {

    /**
     * Record a task creation event
     */
    fun recordTaskCreated(taskId: UUID, changedBy: String)

    /**
     * Record a status change event
     */
    fun recordStatusChange(
        taskId: UUID,
        fromStatus: TaskStatus?,
        toStatus: TaskStatus,
        changedBy: String
    )

    /**
     * Record an assignment change event
     */
    fun recordAssignmentChange(
        taskId: UUID,
        fromAssignee: String?,
        toAssignee: String?,
        changedBy: String
    )

    /**
     * Record an outcome change event
     */
    fun recordOutcomeChange(
        taskId: UUID,
        fromOutcome: String?,
        toOutcome: String?,
        changedBy: String
    )

    /**
     * Record a description change event
     */
    fun recordDescriptionChange(
        taskId: UUID,
        fromDescription: String?,
        toDescription: String?,
        changedBy: String
    )

    /**
     * Record a due date change event
     */
    fun recordDueDateChange(
        taskId: UUID,
        fromDueAt: LocalDateTime?,
        toDueAt: LocalDateTime?,
        changedBy: String
    )

    /**
     * Record completion change event
     */
    fun recordCompletion(
        taskId: UUID,
        changedBy: String
    )

    /**
     * Get complete timeline for a task
     */
    fun getTaskTimeline(taskId: UUID, paginationRequest: PaginationRequest): PaginatedResponse<TaskHistoryResponse>
}
