package com.nivasafinance.features.stages.service

import com.nivasafinance.features.stagedefinitions.repository.StageDefinitionRepositoryWrapper
import com.nivasafinance.features.stages.dto.StageRequest
import com.nivasafinance.features.stages.dto.StageResponse
import com.nivasafinance.features.stages.dto.StageUpdateRequest
import com.nivasafinance.features.stages.entity.Stage
import com.nivasafinance.features.stages.exception.StageExceptionFactory
import com.nivasafinance.features.stages.repository.StageRepositoryWrapper
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
@Transactional
class StageServiceImpl(
    private val stageRepositoryWrapper: StageRepositoryWrapper,
    private val stageDefinitionRepositoryWrapper: StageDefinitionRepositoryWrapper,
    private val messageSource: MessageSource
) : StageService {

    private fun toStage(stageRequest: StageRequest): Stage {
        return Stage(
            stageDefinitionKey = stageRequest.stageDefinitionKey,
            outcome = stageRequest.outcome,
            assignedTo = stageRequest.assignedTo
        )
    }

    private fun toStageResponse(stage: Stage): StageResponse {
        return StageResponse(
            id = stage.id ?: UUID.randomUUID(),
            stageDefinitionKey = stage.stageDefinitionKey,
            outcome = stage.outcome ?: "",
            assignedTo = stage.assignedTo,
            createdAt = stage.createdAt ?: LocalDateTime.now(),
            createdBy = stage.createdBy,
            updatedAt = stage.updatedAt ?: LocalDateTime.now(),
            updatedBy = stage.updatedBy
        )
    }

    override fun createStage(stageRequest: StageRequest): StageResponse {
        val stageDefinition = validateStageDefinitionKey(stageRequest.stageDefinitionKey)
        validateOutcome(stageRequest.outcome, stageDefinition)

        val stage = toStage(stageRequest)
        val savedStage = stageRepositoryWrapper.saveWithException(stage)

        return toStageResponse(savedStage)
    }

    override fun updateStage(stageId: UUID, stageUpdateRequest: StageUpdateRequest): StageResponse {
        val existingStage = stageRepositoryWrapper.findByIdWithException(stageId)

        val stageDefinition = validateStageDefinitionKey(existingStage.stageDefinitionKey)
        validateOutcome(stageUpdateRequest.outcome, stageDefinition)

        val updatedStage = existingStage.copy(
            outcome = stageUpdateRequest.outcome,
            assignedTo = stageUpdateRequest.assignedTo
        )
        val savedStage = stageRepositoryWrapper.saveWithException(updatedStage)
        return toStageResponse(savedStage)
    }

    override fun getStageById(stageId: UUID): StageResponse {
        val stage = stageRepositoryWrapper.findByIdWithException(stageId)
        return toStageResponse(stage)
    }

    override fun getStagesByDefinitionKey(stageDefinitionKey: String): List<StageResponse> {
        val stages = stageRepositoryWrapper.findAllByStageDefinitionKeyWithException(stageDefinitionKey)
        return stages.map { toStageResponse(it) }
    }

    override fun getAllStages(): List<StageResponse> {
        val stages = stageRepositoryWrapper.findAllWithException()
        return stages.map { toStageResponse(it) }
    }

    private fun validateStageDefinitionKey(stageDefinitionKey: String): com.nivasafinance.features.stagedefinitions.entity.StageDefinition {
        val stageDefinition = stageDefinitionRepositoryWrapper.findByKeyWithException(stageDefinitionKey)
        if (stageDefinition == null) {
            throw StageExceptionFactory.invalidStageDefinitionKey(stageDefinitionKey, messageSource)
        }
        return stageDefinition
    }

    private fun validateOutcome(outcome: String, stageDefinition: com.nivasafinance.features.stagedefinitions.entity.StageDefinition) {
        val validOutcomes = stageDefinition.possibleOutcomes?.toSet() ?: emptySet<String>()
        if (validOutcomes.isNotEmpty() && !validOutcomes.contains(outcome)) {
            throw StageExceptionFactory.invalidOutcome(outcome, stageDefinition.key, messageSource)
        }
    }
}
