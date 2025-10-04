package com.nivasafinance.features.tasks.exception

import org.springframework.context.MessageSource
import java.util.UUID

object TaskExceptionFactory {

    fun taskNotFound(taskId: UUID, messageSource: MessageSource): TaskNotFoundException {
        return TaskNotFoundException(taskId, messageSource)
    }

    fun taskKeyAlreadyExists(taskKey: String, messageSource: MessageSource): TaskKeyAlreadyExistsException {
        return TaskKeyAlreadyExistsException(taskKey, messageSource)
    }

    fun taskAlreadyExists(taskDefinitionKey: String, messageSource: MessageSource): TaskKeyAlreadyExistsException {
        return TaskKeyAlreadyExistsException(taskDefinitionKey, messageSource)
    }





    fun dueDatePast(messageSource: MessageSource): TaskDueDatePastException {
        return TaskDueDatePastException(messageSource)
    }

    fun assignmentRequired(status: String, messageSource: MessageSource): TaskAssignmentRequiredException {
        return TaskAssignmentRequiredException(status, messageSource)
    }

    fun cannotUpdateCompleted(taskId: UUID, messageSource: MessageSource): TaskCannotUpdateCompletedException {
        return TaskCannotUpdateCompletedException(taskId, messageSource)
    }

    fun cannotDeleteCompleted(taskId: UUID, messageSource: MessageSource): TaskCannotDeleteCompletedException {
        return TaskCannotDeleteCompletedException(taskId, messageSource)
    }

    fun invalidStatusTransition(currentStatus: String, newStatus: String, messageSource: MessageSource): TaskInvalidStatusTransitionException {
        return TaskInvalidStatusTransitionException(currentStatus, newStatus, messageSource)
    }

    fun createFailed(messageSource: MessageSource): TaskOperationException {
        return TaskOperationException("error.task.operation.create", messageSource)
    }

    fun deleteFailed(messageSource: MessageSource): TaskOperationException {
        return TaskOperationException("error.task.operation.delete", messageSource)
    }

    fun retrieveEntityFailed(messageSource: MessageSource): TaskOperationException {
        return TaskOperationException("error.task.operation.retrieve", messageSource)
    }







}
