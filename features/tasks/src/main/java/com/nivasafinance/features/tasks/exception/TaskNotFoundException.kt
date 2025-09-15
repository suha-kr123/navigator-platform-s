package com.nivasafinance.features.tasks.exception

import exception.ExceptionUtils
import exception.ResourceNotFoundException
import org.springframework.context.MessageSource
import java.util.*

class TaskNotFoundException(
    taskId: UUID,
    messageSource: MessageSource
) : ResourceNotFoundException(
    ExceptionUtils.createLocalizedMessage(
        "error.task.not.found",
        arrayOf(taskId.toString()),
        messageSource
    )
)
