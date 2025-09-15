package com.nivasafinance.features.tasks.service

import com.nivasafinance.features.tasks.entity.TaskStageMapping
import com.nivasafinance.features.tasks.repository.TaskStageMappingRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional
class TaskStageMappingService(
    private val taskStageMappingRepository: TaskStageMappingRepository
) {
    
    fun createTaskStageMapping(taskId: UUID, entityId: UUID, entityType: String, stageKey: String): TaskStageMapping {
        val mapping = TaskStageMapping(
            taskId = taskId,
            entityId = entityId,
            entityType = entityType,
            stageKey = stageKey,
            createdAt = LocalDateTime.now()
        )
        return taskStageMappingRepository.save(mapping)
    }
    
    fun getStageForTask(taskId: UUID): String? {
        return taskStageMappingRepository.findByTaskId(taskId)?.stageKey
    }
    
    fun getTasksForEntityAndStage(entityId: UUID, entityType: String, stageKey: String): List<UUID> {
        return taskStageMappingRepository.findByEntityIdAndEntityTypeAndStageKey(entityId, entityType, stageKey)
            .map { it.taskId }
    }
    
    fun getTasksForEntity(entityId: UUID, entityType: String): List<UUID> {
        return taskStageMappingRepository.findByEntityIdAndEntityType(entityId, entityType)
            .map { it.taskId }
    }
}
