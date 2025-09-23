package com.nivasafinance.features.address.exception

import com.nivasafinance.features.address.enum.AddressType
import exception.ExceptionUtils
import org.springframework.context.MessageSource
import java.util.UUID

@Suppress("TooManyFunctions")
object AddressExceptionFactory {

    private const val PINCODE_LENGTH = 6

    fun notFound(addressId: UUID, messageSource: MessageSource): AddressNotFoundException {
        return AddressNotFoundException(addressId, messageSource)
    }

    @Suppress("UnusedParameter")
    fun notFoundByEntityAndType(
        entityType: String,
        entityId: UUID,
        addressType: AddressType,
        messageSource: MessageSource
    ): AddressNotFoundException {
        return AddressNotFoundException(
            UUID.randomUUID(), // We don't have the actual address ID here
            messageSource
        ).apply {
            // Override the message to be more specific
            this.initCause(
                AddressNotFoundException(
                    UUID.randomUUID(),
                    messageSource
                )
            )
        }
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

    fun entityInvalid(
        entityType: String?,
        entityId: UUID?,
        messageSource: MessageSource
    ): AddressEntityValidationException {
        return AddressEntityValidationException(
            entityType,
            entityId?.toString(),
            messageSource
        )
    }

    fun validateAddressForCreation(
        entityType: String?,
        entityId: UUID?,
        pincode: String,
        messageSource: MessageSource
    ) {
        ExceptionUtils.requireNotBlank(entityType, "entityType", "error.address.entity.type.required", messageSource)
        ExceptionUtils.requireNotNull(entityId, "entityId", "error.address.entity.id.required", messageSource)
        ExceptionUtils.requireNotBlank(pincode, "pincode", "error.address.pincode.required", messageSource)

        validateEntity(entityType, entityId, messageSource)
        validatePincode(pincode, messageSource)
    }

    fun validateAddressForRetrieval(
        entityType: String?,
        entityId: UUID?,
        messageSource: MessageSource
    ) {
        ExceptionUtils.requireNotBlank(entityType, "entityType", "error.address.entity.type.required", messageSource)
        ExceptionUtils.requireNotNull(entityId, "entityId", "error.address.entity.id.required", messageSource)

        validateEntity(entityType, entityId, messageSource)
    }

    fun validateAddressForUpdate(
        addressId: UUID?,
        entityType: String?,
        entityId: UUID?,
        messageSource: MessageSource
    ) {
        ExceptionUtils.requireNotNull(addressId, "addressId", "error.address.id.required", messageSource)
        ExceptionUtils.requireNotBlank(entityType, "entityType", "error.address.entity.type.required", messageSource)
        ExceptionUtils.requireNotNull(entityId, "entityId", "error.address.entity.id.required", messageSource)

        validateEntity(entityType, entityId, messageSource)
    }

    private fun validateEntity(entityType: String?, entityId: UUID?, messageSource: MessageSource) {
        if (entityType.isNullOrBlank()) {
            throw entityInvalid(entityType, entityId, messageSource)
        }
        if (entityId == null) {
            throw entityInvalid(entityType, entityId, messageSource)
        }
    }

    private fun validatePincode(pincode: String, messageSource: MessageSource) {
        if (pincode.isBlank() || pincode.length != PINCODE_LENGTH) {
            throw pincodeInvalid(pincode, messageSource)
        }
    }
}
