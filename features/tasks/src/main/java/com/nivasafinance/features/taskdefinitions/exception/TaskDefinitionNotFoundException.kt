package com.nivasafinance.features.taskdefinitions.exception

import com.nivasafinance.common.exception.ExceptionUtils
import com.nivasafinance.common.exception.ResourceNotFoundException
import org.springframework.context.MessageSource

class TaskDefinitionNotFoundException(
    taskDefinitionKey: String,
    messageSource: MessageSource
) : ResourceNotFoundException(
    ExceptionUtils.createLocalizedMessage(
        "error.task.definition.not.found",
        arrayOf(taskDefinitionKey),
        messageSource
    )
)
