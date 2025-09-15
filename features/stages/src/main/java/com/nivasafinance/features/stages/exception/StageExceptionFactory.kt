package com.nivasafinance.features.stages.exception

import com.nivasafinance.features.stages.enum.EntityType
import com.nivasafinance.features.stages.enum.Status
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

    fun stageKeyAlreadyExists(stageKey: String, messageSource: MessageSource): StageKeyAlreadyExistsException {
        return StageKeyAlreadyExistsException(stageKey, messageSource)
    }

    fun assignmentRequired(status: Status, messageSource: MessageSource): StageAssignmentRequiredException {
        return StageAssignmentRequiredException(status.name, messageSource)
    }

    fun cannotUpdateCompleted(stageId: UUID, messageSource: MessageSource): StageCannotUpdateCompletedException {
        return StageCannotUpdateCompletedException(stageId, messageSource)
    }

    fun cannotDeleteCompleted(stageId: UUID, messageSource: MessageSource): StageCannotDeleteCompletedException {
        return StageCannotDeleteCompletedException(stageId, messageSource)
    }

    fun invalidStatusTransition(currentStatus: Status, newStatus: Status, messageSource: MessageSource): StageInvalidStatusTransitionException {
        return StageInvalidStatusTransitionException(currentStatus.name, newStatus.name, messageSource)
    }

    fun taskNotFoundInStage(taskId: UUID, stageId: UUID, messageSource: MessageSource): StageValidationException {
        return StageValidationException("error.stage.task.not.found", arrayOf(taskId.toString(), stageId.toString()), messageSource)
    }

    fun validateEntityParameters(entityId: UUID?, entityType: EntityType?, messageSource: MessageSource) {
        ExceptionUtils.requireNotNull(entityId, "entityId", "error.invalid", messageSource)
        ExceptionUtils.requireNotNull(entityType, "entityType", "error.invalid", messageSource)
    }

    fun validateStageForCreation(
        entityType: EntityType?,
        entityId: UUID?,
        status: Status?,
        outcome: Any?,
        assignedTo: String?,
        messageSource: MessageSource
    ) {
        ExceptionUtils.requireNotNull(entityType, "entityType", "error.invalid", messageSource)
        ExceptionUtils.requireNotNull(entityId, "entityId", "error.invalid", messageSource)
        ExceptionUtils.requireNotNull(status, "status", "error.invalid", messageSource)
        ExceptionUtils.requireNotNull(outcome, "outcome", "error.invalid", messageSource)

        validateStageAssignment(status, assignedTo, messageSource)
    }

    fun validateStageForUpdate(
        stageId: UUID?,
        entityType: EntityType?,
        entityId: UUID?,
        status: Status?,
        outcome: Any?,
        assignedTo: String?,
        messageSource: MessageSource
    ) {
        ExceptionUtils.requireNotNull(stageId, "id", "error.invalid", messageSource)
        validateStageForCreation(entityType, entityId, status, outcome, assignedTo, messageSource)
    }

    private fun validateStageAssignment(status: Status?, assignedTo: String?, messageSource: MessageSource) {
        if (status in listOf(Status.ACTIVE, Status.PENDING, Status.COMPLETED) && assignedTo.isNullOrBlank()) {
            throw assignmentRequired(status!!, messageSource)
        }
    }
}
