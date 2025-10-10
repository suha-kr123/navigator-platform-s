package com.nivasafinance.features.taskhistory.repository

import com.nivasafinance.features.taskhistory.entity.TaskHistory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface TaskHistoryRepository : JpaRepository<TaskHistory, UUID> {

    /**
     * Find all history events for a task ordered by timestamp (chronological)
     */
    fun findByTaskIdOrderByChangedAtAsc(taskId: UUID): List<TaskHistory>
}
