package com.nivasafinance.features.tasks.exception

import com.nivasafinance.common.exception.ExceptionUtils
import com.nivasafinance.common.exception.ResourceNotFoundException
import org.springframework.context.MessageSource
import java.util.UUID

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
