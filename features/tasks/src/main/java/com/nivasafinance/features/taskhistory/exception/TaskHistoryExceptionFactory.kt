package com.nivasafinance.features.taskhistory.exception

import org.springframework.context.MessageSource
import java.util.UUID

object TaskHistoryExceptionFactory {

    fun notFound(id: UUID, messageSource: MessageSource): TaskHistoryNotFoundException {
        return TaskHistoryNotFoundException(id, messageSource)
    }

    fun createFailed(messageSource: MessageSource): TaskHistoryOperationException {
        return TaskHistoryOperationException("error.task.history.operation.create", messageSource)
    }

    fun updateFailed(messageSource: MessageSource): TaskHistoryOperationException {
        return TaskHistoryOperationException("error.task.history.operation.update", messageSource)
    }

    fun deleteFailed(messageSource: MessageSource): TaskHistoryOperationException {
        return TaskHistoryOperationException("error.task.history.operation.delete", messageSource)
    }

    fun retrieveEntityFailed(messageSource: MessageSource): TaskHistoryOperationException {
        return TaskHistoryOperationException("error.task.history.operation.retrieve", messageSource)
    }
}
