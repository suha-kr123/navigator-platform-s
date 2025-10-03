package com.nivasafinance.features.stages.exception

import exception.ResourceNotFoundException
import org.springframework.context.MessageSource
import java.util.UUID

class StageNotFoundException(
    stageId: UUID,
    messageSource: MessageSource
) : ResourceNotFoundException(
    exception.ExceptionUtils.createLocalizedMessage(
        "error.stage.not.found",
        arrayOf(stageId.toString()),
        messageSource
    )
)
