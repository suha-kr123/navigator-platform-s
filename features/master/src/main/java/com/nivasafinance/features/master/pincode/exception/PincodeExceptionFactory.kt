package com.nivasafinance.features.master.pincode.exception

import com.nivasafinance.common.exception.ExceptionUtils
import org.springframework.context.MessageSource

object PincodeExceptionFactory {

    private const val PINCODE_LENGTH = 6

    fun notFound(pincode: String, messageSource: MessageSource): PincodeNotFoundException {
        return PincodeNotFoundException(pincode, messageSource)
    }

    fun retrieveEntityFailed(messageSource: MessageSource): PincodeOperationException {
        return PincodeOperationException("error.pincode.operation.retrieve", messageSource)
    }

    fun pincodeInvalid(pincode: String?, messageSource: MessageSource): PincodeValidationException {
        return PincodeValidationException(pincode, messageSource)
    }

    fun validatePincode(pincode: String?, messageSource: MessageSource) {
        ExceptionUtils.requireNotBlank(pincode, "pincode", "error.pincode.invalid", messageSource)

        if (pincode.isNullOrBlank() || pincode.length != PINCODE_LENGTH) {
            throw pincodeInvalid(pincode, messageSource)
        }
    }
}
