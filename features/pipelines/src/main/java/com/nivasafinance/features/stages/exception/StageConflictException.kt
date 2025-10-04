package com.nivasafinance.features.stages.exception

import com.nivasafinance.common.exception.ConflictException
import com.nivasafinance.common.exception.ExceptionUtils
import org.springframework.context.MessageSource

open class StageConflictException(
    messageKey: String,
    args: Array<Any>? = null,
    messageSource: MessageSource
) : ConflictException(
    ExceptionUtils.createLocalizedMessage(messageKey, args, messageSource)
)

class StageKeyAlreadyExistsException(
    stageKey: String,
    messageSource: MessageSource
) : StageConflictException(
    "error.stage.key.already.exists",
    arrayOf(stageKey),
    messageSource
)
