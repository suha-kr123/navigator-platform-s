package com.nivasafinance.features.person.exception

import com.nivasafinance.common.exception.ExceptionUtils
import com.nivasafinance.common.exception.ResourceNotFoundException
import org.springframework.context.MessageSource
import java.util.UUID

class PersonNotFoundException(
    personId: UUID,
    messageSource: MessageSource
) : ResourceNotFoundException(
    ExceptionUtils.createLocalizedMessage("error.person.not.found", arrayOf(personId.toString()), messageSource)
)
