package com.nivasafinance.features.address.exception

import exception.ResourceNotFoundException
import org.springframework.context.MessageSource
import java.util.UUID

class AddressNotFoundException(
    addressId: UUID,
    messageSource: MessageSource
) : ResourceNotFoundException(
    exception.ExceptionUtils.createLocalizedMessage(
        "error.address.not.found",
        arrayOf(addressId.toString()),
        messageSource
    )
)
