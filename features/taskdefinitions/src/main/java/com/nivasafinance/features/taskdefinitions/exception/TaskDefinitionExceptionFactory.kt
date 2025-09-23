package com.nivasafinance.features.taskdefinitions.exception

import org.springframework.context.MessageSource

object TaskDefinitionExceptionFactory {

    fun notFound(taskDefinitionKey: String, messageSource: MessageSource): TaskDefinitionNotFoundException {
        return TaskDefinitionNotFoundException(taskDefinitionKey, messageSource)
    }

    fun retrieveFailed(messageSource: MessageSource): TaskDefinitionOperationException {
        return TaskDefinitionOperationException("error.task.definition.operation.retrieve", messageSource)
    }
}
