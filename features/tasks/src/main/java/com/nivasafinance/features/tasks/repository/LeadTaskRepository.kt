package com.nivasafinance.features.tasks.repository

import com.nivasafinance.features.tasks.entity.LeadTask
import com.nivasafinance.features.tasks.enum.TaskStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

@Repository
interface LeadTaskRepository : JpaRepository<LeadTask, UUID> {

    fun findByLeadId(leadId: UUID): List<LeadTask>

    fun findByAssignedTo(assignedTo: String): List<LeadTask>

    fun findByStatus(status: TaskStatus): List<LeadTask>

    fun findByTaskKey(taskKey: String): List<LeadTask>

    fun findByLeadIdAndStatus(leadId: UUID, status: TaskStatus): List<LeadTask>

    fun findByAssignedToAndStatus(assignedTo: String, status: TaskStatus): List<LeadTask>

    @Query("SELECT lt FROM LeadTask lt WHERE lt.dueAt < :currentTime AND lt.status != :completedStatus")
    fun findOverdueTasks(
        @Param("currentTime") currentTime: LocalDateTime,
        @Param("completedStatus") completedStatus: TaskStatus
    ): List<LeadTask>

    @Query("SELECT lt FROM LeadTask lt WHERE lt.leadId = :leadId AND lt.stage = :stage")
    fun findByLeadIdAndStage(@Param("leadId") leadId: UUID, @Param("stage") stage: String): List<LeadTask>

    fun findByLeadIdAndTaskKey(leadId: UUID, taskKey: String): LeadTask?
}
