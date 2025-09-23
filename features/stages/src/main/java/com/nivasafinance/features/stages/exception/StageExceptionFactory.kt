package com.nivasafinance.features.stages.exception

import com.nivasafinance.features.stages.enum.EntityType
import exception.ExceptionUtils
import org.springframework.context.MessageSource
import java.util.*

object StageExceptionFactory {

    fun notFound(stageId: UUID, messageSource: MessageSource): StageNotFoundException {
        return StageNotFoundException(stageId, messageSource)
    }

    fun createFailed(messageSource: MessageSource): StageOperationException {
        return StageOperationException("error.stage.operation.create", messageSource)
    }

    fun updateFailed(messageSource: MessageSource): StageOperationException {
        return StageOperationException("error.stage.operation.update", messageSource)
    }

    fun deleteFailed(messageSource: MessageSource): StageOperationException {
        return StageOperationException("error.stage.operation.delete", messageSource)
    }

    fun retrieveEntityFailed(messageSource: MessageSource): StageOperationException {
        return StageOperationException("error.stage.operation.retrieve", messageSource)
    }

        fun unsupportedEntityType(entityType: String, messageSource: MessageSource): StageOperationException {
            return StageOperationException("error.stage.unsupported.entity.type", messageSource)
        }

        fun invalidStageDefinitionKey(stageDefinitionKey: String, messageSource: MessageSource): StageOperationException {
            return StageOperationException("error.stage.invalid.stage.definition.key", messageSource)
        }

        fun duplicateStageCombination(entityType: String, entityId: UUID, stageDefinitionKey: String, messageSource: MessageSource): StageOperationException {
            return StageOperationException("error.stage.duplicate.combination", messageSource)
        }

        fun invalidOutcome(outcome: String, stageDefinitionKey: String, messageSource: MessageSource): StageOperationException {
            return StageOperationException("error.stage.invalid.outcome", messageSource)
        }

    fun validateStageForCreation(
        entityType: String?,
        entityId: UUID?,
        outcome: Any?,
        assignedTo: String?,
        messageSource: MessageSource
    ) {
        ExceptionUtils.requireNotNull(entityType, "entityType", "error.invalid", messageSource)
        ExceptionUtils.requireNotNull(entityId, "entityId", "error.invalid", messageSource)
        ExceptionUtils.requireNotNull(outcome, "outcome", "error.invalid", messageSource)
    }

}