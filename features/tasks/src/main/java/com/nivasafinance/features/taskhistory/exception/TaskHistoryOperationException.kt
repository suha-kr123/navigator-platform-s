package com.nivasafinance.features.taskhistory.exception

import com.nivasafinance.common.exception.ExceptionUtils
import org.springframework.context.MessageSource

class TaskHistoryOperationException(
    messageKey: String,
    messageSource: MessageSource
) : RuntimeException(
    ExceptionUtils.createLocalizedMessage(messageKey, null, messageSource)
)
