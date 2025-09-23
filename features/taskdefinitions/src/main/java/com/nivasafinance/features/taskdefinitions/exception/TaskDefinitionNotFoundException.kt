package com.nivasafinance.features.taskdefinitions.exception

import exception.ExceptionUtils
import exception.ResourceNotFoundException
import org.springframework.context.MessageSource
import java.util.*

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
