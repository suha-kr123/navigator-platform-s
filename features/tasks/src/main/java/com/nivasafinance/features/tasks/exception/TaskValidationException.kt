package com.nivasafinance.features.tasks.exception

import exception.ExceptionUtils
import exception.ValidationException
import org.springframework.context.MessageSource
import java.util.*

open class TaskValidationException(
    messageKey: String,
    args: Array<Any>? = null,
    messageSource: MessageSource
) : ValidationException(
    ExceptionUtils.createLocalizedMessage(messageKey, args, messageSource)
)

class TaskDueDatePastException(messageSource: MessageSource) : TaskValidationException(
    "error.task.due.date.past",
    null,
    messageSource
)

class TaskAssignmentRequiredException(
    status: String,
    messageSource: MessageSource
) : TaskValidationException(
    "error.task.assignment.required",
    arrayOf(status),
    messageSource
)

class TaskCannotUpdateCompletedException(
    taskId: UUID,
    messageSource: MessageSource
) : TaskValidationException(
    "error.task.cannot.update.completed",
    arrayOf(taskId.toString()),
    messageSource
)

class TaskCannotDeleteCompletedException(
    taskId: UUID,
    messageSource: MessageSource
) : TaskValidationException(
    "error.task.cannot.delete.completed",
    arrayOf(taskId.toString()),
    messageSource
)

class TaskInvalidStatusTransitionException(
    currentStatus: String,
    newStatus: String,
    messageSource: MessageSource
) : TaskValidationException(
    "error.task.invalid.status.transition",
    arrayOf(currentStatus, newStatus),
    messageSource
)
