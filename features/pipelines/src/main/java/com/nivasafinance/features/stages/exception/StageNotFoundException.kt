package com.nivasafinance.features.stages.exception

import com.nivasafinance.common.exception.ExceptionUtils
import com.nivasafinance.common.exception.ResourceNotFoundException
import org.springframework.context.MessageSource
import java.util.UUID

class StageNotFoundException(
    stageId: UUID,
    messageSource: MessageSource
) : ResourceNotFoundException(
    ExceptionUtils.createLocalizedMessage(
        "error.stage.not.found",
        arrayOf(stageId.toString()),
        messageSource
    )
)
