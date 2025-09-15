package com.nivasafinance.features.taskstagemapping.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*
import com.nivasafinance.features.taskstagemapping.entity.TaskStageMapping 

@Repository
interface TaskStageMappingRepository : JpaRepository<TaskStageMapping, UUID> {
    fun findByTaskId(taskId: UUID): TaskStageMapping?
    fun findByEntityIdAndEntityType(entityId: UUID, entityType: String): List<TaskStageMapping>
    fun findByEntityIdAndEntityTypeAndStageKey(entityId: UUID, entityType: String, stageKey: String): List<TaskStageMapping>
}
