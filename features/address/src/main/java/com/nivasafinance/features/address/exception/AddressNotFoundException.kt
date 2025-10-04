package com.nivasafinance.features.address.exception

import com.nivasafinance.common.exception.ExceptionUtils
import com.nivasafinance.common.exception.ResourceNotFoundException
import org.springframework.context.MessageSource
import java.util.UUID

class AddressNotFoundException(
    addressId: UUID,
    messageSource: MessageSource
) : ResourceNotFoundException(
    ExceptionUtils.createLocalizedMessage(
        "error.address.not.found",
        arrayOf(addressId.toString()),
        messageSource
    )
)
