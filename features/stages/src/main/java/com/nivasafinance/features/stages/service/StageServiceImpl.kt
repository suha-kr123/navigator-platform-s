package com.nivasafinance.features.stages.service

import com.nivasafinance.features.stages.dto.StageRequest
import com.nivasafinance.features.stages.dto.StageResponse
import com.nivasafinance.features.stages.dto.StageUpdateRequest
import com.nivasafinance.features.stages.entity.Stage
import com.nivasafinance.features.stages.enum.EntityType
import com.nivasafinance.features.stages.exception.StageExceptionFactory
import com.nivasafinance.features.stages.repository.StageRepositoryWrapper
import com.nivasafinance.features.stagedefinitions.repository.StageDefinitionRepositoryWrapper
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


    private fun toStage(stageRequest: StageRequest, entityType: String, entityId: UUID): Stage {
        return Stage(
            stageDefinitionKey = stageRequest.stageDefinitionKey,
            entityType = entityType,
            entityId = entityId,
            outcome = stageRequest.outcome,
            assignedTo = stageRequest.assignedTo
        )
    }

    private fun toStageResponse(stage: Stage): StageResponse {
        return StageResponse(
            id = stage.id ?: UUID.randomUUID(),
            entityType = stage.entityType,
            entityId = stage.entityId,
            stageDefinitionKey = stage.stageDefinitionKey,
            outcome = stage.outcome ?: "",
            assignedTo = stage.assignedTo,
            createdAt = stage.createdAt ?: LocalDateTime.now(),
            createdBy = stage.createdBy,
            updatedAt = stage.updatedAt ?: LocalDateTime.now(),
            updatedBy = stage.updatedBy
        )
    }


    override fun createStageByEntity(entityType: String, entityId: UUID, stageRequest: StageRequest): StageResponse {
        validateEntityType(entityType)
        
        StageExceptionFactory.validateStageForCreation(
            entityType,
            entityId,
            stageRequest.outcome,
            stageRequest.assignedTo,
            messageSource
        )
        
        val stageDefinition = validateStageDefinitionKey(stageRequest.stageDefinitionKey)
        
        validateOutcome(stageRequest.outcome, stageDefinition)
        
        validateUniqueStageCombination(entityType, entityId, stageRequest.stageDefinitionKey)
        
        val stage = toStage(stageRequest, entityType, entityId)
        val savedStage = stageRepositoryWrapper.saveWithException(stage)
        
        return toStageResponse(savedStage)
    }

    override fun updateStageByEntity(entityType: String, entityId: UUID, stageId: UUID, stageUpdateRequest: StageUpdateRequest): StageResponse {
        validateEntityType(entityType)
        
        val existingStages = stageRepositoryWrapper.findAllByEntityTypeAndEntityIdWithException(
            entityType, 
            entityId
        )
        val existingStage = existingStages.firstOrNull { it.id == stageId }
        
        if (existingStage == null) {
            throw StageExceptionFactory.notFound(stageId, messageSource)
        }
        
        val stageDefinition = validateStageDefinitionKey(existingStage.stageDefinitionKey)
        validateOutcome(stageUpdateRequest.outcome, stageDefinition)
        
        val updatedStage = existingStage.copy(
            outcome = stageUpdateRequest.outcome,
            assignedTo = stageUpdateRequest.assignedTo
        )
        val savedStage = stageRepositoryWrapper.saveWithException(updatedStage)
        return toStageResponse(savedStage)
    }

    override fun getStagesByEntity(entityType: String, entityId: UUID): List<StageResponse> {
        validateEntityType(entityType)
        
        val stages = stageRepositoryWrapper.findAllByEntityTypeAndEntityIdWithException(entityType, entityId)
        return stages.map { stage ->
            toStageResponse(stage)
        }
    }

    private fun validateEntityType(entityType: String) {
        try {
            EntityType.valueOf(entityType.uppercase())
        } catch (e: IllegalArgumentException) {
            throw StageExceptionFactory.unsupportedEntityType(entityType, messageSource)
        }
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

    private fun validateUniqueStageCombination(entityType: String, entityId: UUID, stageDefinitionKey: String) {
        val exists = stageRepositoryWrapper.existsByEntityTypeAndEntityIdAndStageDefinitionKeyWithException(
            entityType, entityId, stageDefinitionKey
        )
        if (exists) {
            throw StageExceptionFactory.duplicateStageCombination(entityType, entityId, stageDefinitionKey, messageSource)
        }
    }

}