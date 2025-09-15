package com.nivasafinance.features.stagetasks.service

import com.nivasafinance.features.stagetasks.dto.StageTaskRequest
import com.nivasafinance.features.stagetasks.dto.StageTaskResponse
import com.nivasafinance.features.stagetasks.entity.StageTask
import com.nivasafinance.features.stagetasks.repository.StageTaskRepositoryWrapper
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class StageTaskServiceImpl(
    private val stageTaskRepositoryWrapper: StageTaskRepositoryWrapper
) : StageTaskService {

    override fun createStageTask(stageTaskRequest: StageTaskRequest): StageTaskResponse {
        val stageTask = StageTask(
            stageId = stageTaskRequest.stageId,
            taskId = stageTaskRequest.taskId,
            extData = stageTaskRequest.extData
        )
        val savedStageTask = stageTaskRepositoryWrapper.saveWithException(stageTask)
        return StageTaskResponse(
            id = savedStageTask.id!!,
            stageId = savedStageTask.stageId,
            taskId = savedStageTask.taskId,
            extData = savedStageTask.extData
        )
    }

    override fun getTasksForStage(stageId: UUID): List<StageTaskResponse> {
        val stageTasks = stageTaskRepositoryWrapper.findAllByStageIdWithException(stageId)
        return stageTasks.map { stageTask ->
            StageTaskResponse(
                id = stageTask.id!!,
                stageId = stageTask.stageId,
                taskId = stageTask.taskId,
                extData = stageTask.extData
            )
        }
    }

    override fun deleteStageTask(stageId: UUID, taskId: UUID) {
        stageTaskRepositoryWrapper.deleteByStageIdAndTaskIdWithException(stageId, taskId)
    }
}