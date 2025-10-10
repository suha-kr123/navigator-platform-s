package com.nivasafinance.features.taskhistory.service.impl

import com.nivasafinance.common.base.model.PaginatedResponse
import com.nivasafinance.common.base.model.PaginationRequest
import com.nivasafinance.features.taskhistory.dto.TaskHistoryResponse
import com.nivasafinance.features.taskhistory.entity.TaskHistory
import com.nivasafinance.features.taskhistory.enum.TaskEventType
import com.nivasafinance.features.taskhistory.repository.TaskHistoryRepositoryWrapper
import com.nivasafinance.features.taskhistory.service.TaskHistoryService
import com.nivasafinance.features.tasks.enum.TaskStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
@Transactional
class TaskHistoryServiceImpl(
    private val taskHistoryRepositoryWrapper: TaskHistoryRepositoryWrapper
) : TaskHistoryService {

    override fun recordTaskCreated(taskId: UUID, changedBy: String) {
        val history = TaskHistory(
            taskId = taskId,
            eventType = TaskEventType.CREATED,
            oldValue = null,
            newValue = null,
            changedBy = changedBy,
            changedAt = LocalDateTime.now()
        )
        taskHistoryRepositoryWrapper.save(history)
    }

    override fun recordStatusChange(
        taskId: UUID,
        fromStatus: TaskStatus?,
        toStatus: TaskStatus,
        changedBy: String
    ) {
        val history = TaskHistory(
            taskId = taskId,
            eventType = TaskEventType.STATUS_CHANGED,
            oldValue = fromStatus?.value,
            newValue = toStatus.value,
            changedBy = changedBy,
            changedAt = LocalDateTime.now(),
        )
        taskHistoryRepositoryWrapper.save(history)
    }

    override fun recordAssignmentChange(
        taskId: UUID,
        fromAssignee: String?,
        toAssignee: String?,
        changedBy: String
    ) {
        val history = TaskHistory(
            taskId = taskId,
            eventType = TaskEventType.ASSIGNED,
            oldValue = fromAssignee,
            newValue = toAssignee,
            changedBy = changedBy,
            changedAt = LocalDateTime.now(),
        )
        taskHistoryRepositoryWrapper.save(history)
    }

    override fun recordOutcomeChange(
        taskId: UUID,
        fromOutcome: String?,
        toOutcome: String?,
        changedBy: String
    ) {
        val history = TaskHistory(
            taskId = taskId,
            eventType = TaskEventType.OUTCOME_SET,
            oldValue = fromOutcome,
            newValue = toOutcome,
            changedBy = changedBy,
            changedAt = LocalDateTime.now(),
        )
        taskHistoryRepositoryWrapper.save(history)
    }

    override fun recordDescriptionChange(
        taskId: UUID,
        fromDescription: String?,
        toDescription: String?,
        changedBy: String
    ) {
        val history = TaskHistory(
            taskId = taskId,
            eventType = TaskEventType.DESCRIPTION_UPDATED,
            oldValue = fromDescription,
            newValue = toDescription,
            changedBy = changedBy,
            changedAt = LocalDateTime.now(),
        )
        taskHistoryRepositoryWrapper.save(history)
    }

    override fun recordDueDateChange(
        taskId: UUID,
        fromDueAt: LocalDateTime?,
        toDueAt: LocalDateTime?,
        changedBy: String
    ) {
        val history = TaskHistory(
            taskId = taskId,
            eventType = TaskEventType.RESCHEDULED,
            oldValue = fromDueAt?.toString(),
            newValue = toDueAt?.toString(),
            changedBy = changedBy,
            changedAt = LocalDateTime.now(),
        )
        taskHistoryRepositoryWrapper.save(history)
    }

    override fun recordCompletion(
        taskId: UUID,
        changedBy: String
    ) {
        val history = TaskHistory(
            taskId = taskId,
            eventType = TaskEventType.COMPLETED,
            oldValue = null,
            newValue = null,
            changedBy = changedBy,
            changedAt = LocalDateTime.now(),
        )
        taskHistoryRepositoryWrapper.save(history)
    }

    @Transactional(readOnly = true)
    override fun getTaskTimeline(
        taskId: UUID,
        paginationRequest: PaginationRequest
    ): PaginatedResponse<TaskHistoryResponse> {
        val allHistory = taskHistoryRepositoryWrapper.findByTaskIdOrderByChangedAtAsc(taskId)
        val totalElements = allHistory.size.toLong()

        val paginatedHistory = allHistory
            .drop(paginationRequest.offset)
            .take(paginationRequest.limit)
            .map { it.toResponse() }

        val totalPages = if (totalElements > 0) ((totalElements - 1) / paginationRequest.limit + 1).toInt() else 0
        val currentPage = paginationRequest.offset / paginationRequest.limit

        val paginationInfo = com.nivasafinance.common.base.model.PaginationInfo(
            offset = paginationRequest.offset,
            limit = paginationRequest.limit,
            totalElements = totalElements,
            totalPages = totalPages,
            currentPage = currentPage,
            hasNext = currentPage < totalPages - 1,
            hasPrevious = currentPage > 0
        )

        return PaginatedResponse(
            content = paginatedHistory,
            pagination = paginationInfo
        )
    }

    private fun TaskHistory.toResponse(): TaskHistoryResponse {
        return TaskHistoryResponse(
            id = this.id!!,
            taskId = this.taskId,
            eventType = this.eventType,
            oldValue = this.oldValue,
            newValue = this.newValue,
            changedBy = this.changedBy,
            changedAt = this.changedAt
        )
    }
}
