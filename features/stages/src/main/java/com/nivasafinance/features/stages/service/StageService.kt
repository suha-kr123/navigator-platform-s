package com.nivasafinance.features.stages.service

import com.nivasafinance.features.stages.dto.StageRequest
import com.nivasafinance.features.stages.dto.StageResponse
import com.nivasafinance.features.stages.dto.StageUpdateRequest
import java.util.UUID


interface StageService {

    fun createStageByEntity(entityType: String, entityId: UUID, stageRequest: StageRequest): StageResponse

    fun updateStageByEntity(entityType: String, entityId: UUID, stageId: UUID, stageUpdateRequest: StageUpdateRequest): StageResponse

    fun getStagesByEntity(entityType: String, entityId: UUID): List<StageResponse>
}