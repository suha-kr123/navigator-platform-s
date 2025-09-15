package com.nivasafinance.features.tasks.exception

import exception.ConflictException
import exception.ExceptionUtils
import org.springframework.context.MessageSource

open class TaskConflictException(
    messageKey: String,
    args: Array<Any>? = null,
    messageSource: MessageSource
) : ConflictException(
    ExceptionUtils.createLocalizedMessage(messageKey, args, messageSource)
)

class TaskKeyAlreadyExistsException(
    taskKey: String,
    messageSource: MessageSource
) : TaskConflictException(
    "error.task.key.already.exists",
    arrayOf(taskKey),
    messageSource
)
