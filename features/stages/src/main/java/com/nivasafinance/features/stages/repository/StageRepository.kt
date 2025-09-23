package com.nivasafinance.features.stages.repository

import com.nivasafinance.features.stages.entity.Stage   
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface StageRepository : JpaRepository<Stage, UUID> {

    fun findAllByEntityTypeAndEntityId(entityType: String, entityId: UUID): List<Stage>

    fun existsByEntityTypeAndEntityIdAndStageDefinitionKey(entityType: String, entityId: UUID, stageDefinitionKey: String): Boolean
}
