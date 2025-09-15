package com.nivasafinance.features.stages.repository

import com.nivasafinance.features.stages.entity.Stage
import com.nivasafinance.features.stages.enum.EntityType
import com.nivasafinance.features.stages.enum.Outcome
import com.nivasafinance.features.stages.enum.Status
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface StageRepository : JpaRepository<Stage, UUID> {

    fun findAllByEntityTypeAndEntityId(entityType: EntityType, entityId: UUID): List<Stage>

    fun findByStageKey(stageKey: String): Stage
}
