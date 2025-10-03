package com.nivasafinance.features.stagedefinitions.exception

import exception.ExceptionUtils
import exception.ResourceNotFoundException
import org.springframework.context.MessageSource

class StageDefinitionNotFoundException(
    stageDefinitionKey: String,
    messageSource: MessageSource
) : ResourceNotFoundException(
    ExceptionUtils.createLocalizedMessage(
        "error.stage.definition.not.found",
        arrayOf(stageDefinitionKey),
        messageSource
    )
)
