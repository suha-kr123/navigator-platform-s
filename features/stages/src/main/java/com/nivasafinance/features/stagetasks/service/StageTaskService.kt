package com.nivasafinance.features.stagetasks.service

import com.nivasafinance.features.stagetasks.dto.StageTaskResponse
import com.nivasafinance.features.stagetasks.dto.StageTaskRequest
import java.util.*

interface StageTaskService {

    fun createStageTask(stageTaskRequest: StageTaskRequest): StageTaskResponse
    fun getTasksForStage(stageId: UUID): List<StageTaskResponse>
    fun deleteStageTask(stageId: UUID, taskId: UUID)
}