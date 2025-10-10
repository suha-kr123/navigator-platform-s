package com.nivasafinance.features.taskhistory.exception

import com.nivasafinance.common.exception.ResourceNotFoundException
import org.springframework.context.MessageSource
import java.util.Locale
import java.util.UUID

class TaskHistoryNotFoundException(
    id: UUID,
    messageSource: MessageSource
) : ResourceNotFoundException(
    messageSource.getMessage("error.task.history.not.found", arrayOf(id), Locale.getDefault())
)
