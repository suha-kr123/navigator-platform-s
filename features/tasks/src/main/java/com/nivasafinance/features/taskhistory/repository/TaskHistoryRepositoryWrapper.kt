package com.nivasafinance.features.taskhistory.repository

import com.nivasafinance.features.taskhistory.entity.TaskHistory
import com.nivasafinance.features.taskhistory.exception.TaskHistoryExceptionFactory
import org.springframework.context.MessageSource
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class TaskHistoryRepositoryWrapper(
    private val taskHistoryRepository: TaskHistoryRepository,
    private val messageSource: MessageSource
) {

    /**
     * Save a task history record
     */
    fun save(taskHistory: TaskHistory): TaskHistory {
        return try {
            taskHistoryRepository.save(taskHistory)
        } catch (e: Exception) {
            throw TaskHistoryExceptionFactory.createFailed(messageSource)
        }
    }

    /**
     * Find all history events for a task ordered by timestamp (chronological)
     */
    fun findByTaskIdOrderByChangedAtAsc(taskId: UUID): List<TaskHistory> {
        return try {
            taskHistoryRepository.findByTaskIdOrderByChangedAtAsc(taskId)
        } catch (e: Exception) {
            throw TaskHistoryExceptionFactory.retrieveEntityFailed(messageSource)
        }
    }
}
