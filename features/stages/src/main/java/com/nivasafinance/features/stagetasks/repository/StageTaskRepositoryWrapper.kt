package com.nivasafinance.features.stagetasks.repository

import com.nivasafinance.features.stagetasks.entity.StageTask
import com.nivasafinance.features.stagetasks.exception.StageTaskExceptionFactory
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class StageTaskRepositoryWrapper(
    private val stageTaskRepository: StageTaskRepository,
    private val messageSource: MessageSource
) { 

    fun saveWithException(stageTask: StageTask): StageTask {
        return try {
            stageTaskRepository.save(stageTask)
        } catch (e: Exception) {
            throw StageTaskExceptionFactory.stageTaskOperationFailed("Failed to create stage task")
        }
    }

    fun findAllByStageIdWithException(stageId: UUID): List<StageTask> {
        return try {
            stageTaskRepository.findAllByStageId(stageId)
        } catch (e: Exception) {
            throw StageTaskExceptionFactory.stageTaskOperationFailed("Failed to retrieve stage tasks")
        }
    }

    fun deleteByStageIdAndTaskIdWithException(stageId: UUID, taskId: UUID) {
        return try {
            stageTaskRepository.deleteByStageIdAndTaskId(stageId, taskId)
        } catch (e: Exception) {
            throw StageTaskExceptionFactory.stageTaskOperationFailed("Failed to delete stage task")
        }
    }

}