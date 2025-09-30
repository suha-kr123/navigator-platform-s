package com.nivasafinance.features.stages.exception

import exception.ExceptionUtils
import exception.ValidationException
import org.springframework.context.MessageSource
import java.util.*

open class StageValidationException(
    messageKey: String,
    args: Array<Any>? = null,
    messageSource: MessageSource
) : ValidationException(
    ExceptionUtils.createLocalizedMessage(messageKey, args, messageSource)
)

class StageAssignmentRequiredException(
    status: String,
    messageSource: MessageSource
) : StageValidationException(
    "error.stage.assignment.required",
    arrayOf(status),
    messageSource
)

class StageInvalidStatusTransitionException(
    currentStatus: String,
    newStatus: String,
    messageSource: MessageSource
) : StageValidationException(
    "error.stage.invalid.status.transition",
    arrayOf(currentStatus, newStatus),
    messageSource
)

class StageCannotUpdateCompletedException(
    stageId: UUID,
    messageSource: MessageSource
) : StageValidationException(
    "error.stage.cannot.update.completed",
    arrayOf(stageId.toString()),
    messageSource
)

class StageCannotDeleteCompletedException(
    stageId: UUID,
    messageSource: MessageSource
) : StageValidationException(
    "error.stage.cannot.delete.completed",
    arrayOf(stageId.toString()),
    messageSource
)
