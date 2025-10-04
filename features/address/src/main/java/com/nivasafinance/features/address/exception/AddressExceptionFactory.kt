package com.nivasafinance.features.address.exception

import com.nivasafinance.common.exception.ExceptionUtils
import org.springframework.context.MessageSource
import java.util.UUID

@Suppress("TooManyFunctions")
object AddressExceptionFactory {

    private const val PINCODE_LENGTH = 6

    fun notFound(addressId: UUID, messageSource: MessageSource): AddressNotFoundException {
        return AddressNotFoundException(addressId, messageSource)
    }

    fun createFailed(messageSource: MessageSource): AddressOperationException {
        return AddressOperationException("error.address.operation.create", messageSource)
    }

    fun updateFailed(messageSource: MessageSource): AddressOperationException {
        return AddressOperationException("error.address.operation.update", messageSource)
    }

    fun deleteFailed(messageSource: MessageSource): AddressOperationException {
        return AddressOperationException("error.address.operation.delete", messageSource)
    }

    fun retrieveEntityFailed(messageSource: MessageSource): AddressOperationException {
        return AddressOperationException("error.address.operation.retrieve", messageSource)
    }

    fun pincodeInvalid(pincode: String?, messageSource: MessageSource): AddressPincodeValidationException {
        return AddressPincodeValidationException(pincode, messageSource)
    }

    fun validateAddressForCreation(
        pincode: String,
        messageSource: MessageSource
    ) {
        ExceptionUtils.requireNotBlank(pincode, "pincode", "error.address.pincode.required", messageSource)

        validatePincode(pincode, messageSource)
    }

    fun validateAddressForRetrieval(
        addressId: UUID?,
        pincode: String,
        messageSource: MessageSource
    ) {
        ExceptionUtils.requireNotNull(addressId, "addressId", "error.address.id.required", messageSource)

        validatePincode(pincode, messageSource)
    }

    fun validateAddressForUpdate(
        addressId: UUID?,
        messageSource: MessageSource
    ) {
        ExceptionUtils.requireNotNull(addressId, "addressId", "error.address.id.required", messageSource)
    }

    private fun validatePincode(pincode: String, messageSource: MessageSource) {
        if (pincode.isBlank() || pincode.length != PINCODE_LENGTH) {
            throw pincodeInvalid(pincode, messageSource)
        }
    }
}
