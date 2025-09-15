package com.nivasafinance.features.stagetasks.repository

import com.nivasafinance.features.stagetasks.entity.StageTask
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface StageTaskRepository : JpaRepository<StageTask, UUID> {

    fun findAllByStageId(stageId: UUID): List<StageTask>

    fun deleteByStageIdAndTaskId(stageId: UUID, taskId: UUID)
}   