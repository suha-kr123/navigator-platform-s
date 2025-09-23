package com.nivasafinance.features.tasks.exception

import com.nivasafinance.features.tasks.enum.TaskStatus
import exception.ExceptionUtils
import org.springframework.context.MessageSource
import java.time.LocalDateTime
import java.util.*

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

    fun unsupportedEntityType(entityType: String, messageSource: MessageSource): TaskOperationException {
        return TaskOperationException("error.task.unsupported.entity.type", messageSource)
    }

    fun operationFailed(operation: String, messageSource: MessageSource): TaskOperationException {
        return TaskOperationException(operation, messageSource)
    }

    fun retrieveEntityFailed(messageSource: MessageSource): TaskOperationException {
        return TaskOperationException("error.task.operation.retrieve.entity", messageSource)
    }

    fun retrieveEntityTypeFailed(messageSource: MessageSource): TaskOperationException {
        return TaskOperationException("error.task.operation.retrieve.entity.type", messageSource)
    }

    fun createFailed(messageSource: MessageSource): TaskOperationException {
        return TaskOperationException("error.task.operation.create", messageSource)
    }

    fun updateFailed(messageSource: MessageSource): TaskOperationException {
        return TaskOperationException("error.task.operation.update", messageSource)
    }

    fun deleteFailed(messageSource: MessageSource): TaskOperationException {
        return TaskOperationException("error.task.operation.delete", messageSource)
    }

    fun dueDatePast(messageSource: MessageSource): TaskDueDatePastException {
        return TaskDueDatePastException(messageSource)
    }

    fun assignmentRequired(status: TaskStatus, messageSource: MessageSource): TaskAssignmentRequiredException {
        return TaskAssignmentRequiredException(status.name, messageSource)
    }

    fun cannotUpdateCompleted(taskId: UUID, messageSource: MessageSource): TaskCannotUpdateCompletedException {
        return TaskCannotUpdateCompletedException(taskId, messageSource)
    }

    fun cannotDeleteCompleted(taskId: UUID, messageSource: MessageSource): TaskCannotDeleteCompletedException {
        return TaskCannotDeleteCompletedException(taskId, messageSource)
    }

    fun invalidStatusTransition(currentStatus: TaskStatus, newStatus: TaskStatus, messageSource: MessageSource): TaskInvalidStatusTransitionException {
        return TaskInvalidStatusTransitionException(currentStatus.name, newStatus.name, messageSource)
    }

    fun validateEntityParameters(entityId: UUID?, entityType: String?, messageSource: MessageSource) {
        ExceptionUtils.requireNotNull(entityId, "entityId", "error.invalid", messageSource)
        ExceptionUtils.requireNotBlank(entityType, "entityType", "error.invalid", messageSource)
    }

    fun validateEntityType(entityType: String?, messageSource: MessageSource) {
        ExceptionUtils.requireNotBlank(entityType, "entityType", "error.invalid", messageSource)
    }

    fun validatePaginationRequest(limit: Int, offset: Int, messageSource: MessageSource) {
        ExceptionUtils.requireTrue(
            limit > 0,
            "error.invalid",
            arrayOf("Pagination limit must be greater than 0"),
            messageSource
        )
        ExceptionUtils.requireTrue(
            offset >= 0,
            "error.invalid",
            arrayOf("Pagination offset must be non-negative"),
            messageSource
        )
    }

    fun validateTaskForCreation(
        taskDefinitionKey: String?,
        taskKey: String?,
        status: TaskStatus?,
        outcome: Any?,
        dueAt: LocalDateTime?,
        assignedTo: String?,
        messageSource: MessageSource
    ) {
        ExceptionUtils.requireNotBlank(taskDefinitionKey, "taskDefinitionKey", "error.invalid", messageSource)
        ExceptionUtils.requireNotBlank(taskKey, "taskKey", "error.invalid", messageSource)
        ExceptionUtils.requireNotNull(status, "status", "error.invalid", messageSource)
        ExceptionUtils.requireNotNull(outcome, "outcome", "error.invalid", messageSource)

        validateDueDate(dueAt, messageSource)
        validateTaskAssignment(status, assignedTo, messageSource)
    }

    fun validateTaskForUpdate(
        taskId: UUID?,
        taskDefinitionKey: String?,
        taskKey: String?,
        status: TaskStatus?,
        outcome: Any?,
        dueAt: LocalDateTime?,
        assignedTo: String?,
        messageSource: MessageSource
    ) {
        ExceptionUtils.requireNotNull(taskId, "id", "error.invalid", messageSource)
        validateTaskForCreation(taskDefinitionKey, taskKey, status, outcome, dueAt, assignedTo, messageSource)
    }

    private fun validateDueDate(dueAt: LocalDateTime?, messageSource: MessageSource) {
        if (dueAt != null && dueAt.isBefore(LocalDateTime.now())) {
            throw dueDatePast(messageSource)
        }
    }

    private fun validateTaskAssignment(status: TaskStatus?, assignedTo: String?, messageSource: MessageSource) {
        if (status in listOf(TaskStatus.IN_PROGRESS, TaskStatus.COMPLETED) && assignedTo.isNullOrBlank()) {
            throw assignmentRequired(status!!, messageSource)
        }
    }
}
