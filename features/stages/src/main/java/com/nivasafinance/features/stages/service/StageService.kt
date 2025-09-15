package com.nivasafinance.features.stages.service

import com.nivasafinance.features.stages.dto.StageRequest
import com.nivasafinance.features.stages.dto.StageResponse
import com.nivasafinance.features.stages.dto.StageUpdateRequest
import com.nivasafinance.features.stages.enum.EntityType
import com.nivasafinance.features.tasks.dto.TaskRequest
import com.nivasafinance.features.tasks.dto.TaskResponse
import com.nivasafinance.features.tasks.dto.UpdateTaskRequest
import java.util.UUID


interface StageService {

    fun createStage(stageRequest: StageRequest): StageResponse

    fun updateStage(stageId: UUID, stageUpdateRequest: StageUpdateRequest): StageResponse

    fun getStagesByEntityTypeAndEntityId(entityType: EntityType, entityId: UUID): List<StageResponse>

    fun getStageById(id: UUID): StageResponse

    fun addTasksToStage(stageId: UUID, tasks: List<TaskRequest>): List<TaskResponse>
    
    fun updateTaskInStage(stageId: UUID, taskId: UUID, updateTaskRequest: UpdateTaskRequest): TaskResponse
    
    fun deleteTaskFromStage(stageId: UUID, taskId: UUID)
    
    fun getTasksForStage(stageId: UUID): List<TaskResponse>
}