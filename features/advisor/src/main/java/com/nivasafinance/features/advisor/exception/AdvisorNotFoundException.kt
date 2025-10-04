package com.nivasafinance.features.advisor.exception

import com.nivasafinance.common.exception.ExceptionUtils
import com.nivasafinance.common.exception.ResourceNotFoundException
import org.springframework.context.MessageSource
import java.util.UUID

class AdvisorNotFoundException(
    advisorId: UUID,
    messageSource: MessageSource
) : ResourceNotFoundException(
    ExceptionUtils.createLocalizedMessage("error.advisor.id.not.found", arrayOf(advisorId.toString()), messageSource)
)
