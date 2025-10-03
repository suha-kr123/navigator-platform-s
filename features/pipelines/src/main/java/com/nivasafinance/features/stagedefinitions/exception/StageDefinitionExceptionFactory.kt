package com.nivasafinance.features.stagedefinitions.exception

import org.springframework.context.MessageSource

object StageDefinitionExceptionFactory {

    fun notFound(stageDefinitionKey: String, messageSource: MessageSource): StageDefinitionNotFoundException {
        return StageDefinitionNotFoundException(stageDefinitionKey, messageSource)
    }

    fun retrieveFailed(messageSource: MessageSource): StageDefinitionOperationException {
        return StageDefinitionOperationException("error.stage.definition.operation.retrieve", messageSource)
    }
}
