package com.nivasafinance.features.stages.exception

import exception.ExceptionUtils
import org.springframework.context.MessageSource
import java.util.UUID

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

    fun invalidStageDefinitionKey(stageDefinitionKey: String, messageSource: MessageSource): StageOperationException {
        return StageOperationException("error.stage.invalid.stage.definition.key", messageSource)
    }

    fun invalidOutcome(outcome: String, stageDefinitionKey: String, messageSource: MessageSource): StageOperationException {
        return StageOperationException("error.stage.invalid.outcome", messageSource)
    }

    fun validateStageForCreation(
        outcome: Any?,
        messageSource: MessageSource
    ) {
        ExceptionUtils.requireNotNull(outcome, "outcome", "error.invalid", messageSource)
    }

}