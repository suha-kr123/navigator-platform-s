package com.nivasafinance.features.stages.service

import com.nivasafinance.features.stages.dto.StageRequest
import com.nivasafinance.features.stages.dto.StageResponse
import com.nivasafinance.features.stages.dto.StageUpdateRequest
import java.util.UUID

interface StageService {

    fun createStage(stageRequest: StageRequest): StageResponse

    fun updateStage(stageId: UUID, stageUpdateRequest: StageUpdateRequest): StageResponse

    fun getStageById(stageId: UUID): StageResponse

    fun getStagesByDefinitionKey(stageDefinitionKey: String): List<StageResponse>

    fun getAllStages(): List<StageResponse>
}
