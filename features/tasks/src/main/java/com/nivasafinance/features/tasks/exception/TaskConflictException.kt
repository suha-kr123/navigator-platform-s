package com.nivasafinance.features.tasks.exception

import com.nivasafinance.common.exception.ConflictException
import com.nivasafinance.common.exception.ExceptionUtils
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
