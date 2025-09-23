package com.nivasafinance.features.address.service.impl

import base.BaseNavigatorService
import com.nivasafinance.features.address.dto.AddressCreateRequest
import com.nivasafinance.features.address.dto.AddressResponse
import com.nivasafinance.features.address.dto.AddressUpdateRequest
import com.nivasafinance.features.address.entity.Address
import com.nivasafinance.features.address.enum.AddressType
import com.nivasafinance.features.address.exception.AddressExceptionFactory
import com.nivasafinance.features.address.repository.AddressRepository
import com.nivasafinance.features.address.service.AddressService
import com.nivasafinance.features.master.pincode.service.PincodeService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class AddressServiceImpl(
    private val addressRepository: AddressRepository,
    private val pincodeService: PincodeService
) : AddressService, BaseNavigatorService() {

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    override fun getAddressesByEntityTypeAndEntityId(entityType: String, entityId: UUID): List<AddressResponse> {
        return try {
            AddressExceptionFactory.validateAddressForRetrieval(entityType, entityId, messageSource)
            val addresses = addressRepository.findByEntityTypeAndEntityId(entityType, entityId)
            addresses.map { toAddressResponse(it) }
        } catch (e: com.nivasafinance.features.address.exception.AddressValidationException) {
            // Re-throw validation exceptions as-is
            throw e
        } catch (e: Exception) {
            // Return empty list instead of throwing exception for retrieval operations
            emptyList()
        }
    }

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    override fun getAddressByEntityTypeAndEntityIdAndAddressType(
        entityType: String,
        entityId: UUID,
        addressType: AddressType
    ): AddressResponse? {
        return try {
            AddressExceptionFactory.validateAddressForRetrieval(entityType, entityId, messageSource)
            val address = addressRepository.findByEntityTypeAndEntityIdAndAddressType(entityType, entityId, addressType)
            address?.let { toAddressResponse(it) }
        } catch (e: com.nivasafinance.features.address.exception.AddressValidationException) {
            // Re-throw validation exceptions as-is
            throw e
        } catch (e: Exception) {
            // Return null instead of throwing exception for retrieval operations
            null
        }
    }

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    override fun getAddressByEntityTypeAndEntityIdAndId(
        entityType: String,
        entityId: UUID,
        addressId: UUID
    ): AddressResponse? {
        return try {
            AddressExceptionFactory.validateAddressForRetrieval(entityType, entityId, messageSource)
            val address = addressRepository.findByEntityTypeAndEntityIdAndId(entityType, entityId, addressId)
            address?.let { toAddressResponse(it) }
        } catch (e: com.nivasafinance.features.address.exception.AddressValidationException) {
            // Re-throw validation exceptions as-is
            throw e
        } catch (e: Exception) {
            // Return null instead of throwing exception for retrieval operations
            null
        }
    }

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    override fun createAddress(
        entityType: String,
        entityId: UUID,
        addressRequest: AddressCreateRequest
    ): AddressResponse {
        return try {
            AddressExceptionFactory.validateAddressForCreation(
                entityType,
                entityId,
                addressRequest.pincode,
                messageSource
            )

            // Try to get pincode details from master data (safe method that never throws)
            val pincodeDetails = pincodeService.getPincodeDetailsSafe(addressRequest.pincode)

            // Determine which data to use
            val (finalDistrict, finalState) = if (pincodeDetails.isNotEmpty()) {
                // Use master data if available
                val masterData = pincodeDetails.first()
                Pair(masterData.district ?: addressRequest.district, masterData.state ?: addressRequest.state)
            } else {
                // Use customer entered data if pincode not in master
                Pair(addressRequest.district, addressRequest.state)
            }

            val address = Address(
                entityId = entityId,
                entityType = entityType,
                addressType = addressRequest.addressType,
                isPrimary = addressRequest.isPrimary,
                addressOne = addressRequest.addressOne,
                addressTwo = addressRequest.addressTwo,
                landmark = addressRequest.landmark,
                district = finalDistrict,
                state = finalState,
                pincode = addressRequest.pincode,
                addressSource = addressRequest.addressSource
            )

            val savedAddress = addressRepository.save(address)
            toAddressResponse(savedAddress)
        } catch (e: com.nivasafinance.features.address.exception.AddressValidationException) {
            // Re-throw validation exceptions as-is
            throw e
        } catch (e: Exception) {
            // Only catch unexpected exceptions
            throw AddressExceptionFactory.createFailed(messageSource)
        }
    }

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    override fun updateAddress(
        entityType: String,
        entityId: UUID,
        addressId: UUID,
        addressUpdateRequest: AddressUpdateRequest
    ): AddressResponse {
        return try {
            AddressExceptionFactory.validateAddressForUpdate(addressId, entityType, entityId, messageSource)

            val address = addressRepository.findByEntityTypeAndEntityIdAndId(entityType, entityId, addressId)
                ?: throw AddressExceptionFactory.notFound(addressId, messageSource)

            // Handle pincode update if provided
            addressUpdateRequest.pincode?.let { pincode ->
                // Try to get pincode details from master data (safe method that never throws)
                val pincodeDetails = pincodeService.getPincodeDetailsSafe(pincode)

                // Update pincode
                address.pincode = pincode

                // Update district and state based on master data availability
                if (pincodeDetails.isNotEmpty()) {
                    // Use master data if available
                    val masterData = pincodeDetails.first()
                    masterData.district?.let { address.district = it }
                    masterData.state?.let { address.state = it }
                } else {
                    // Use customer entered data if pincode not in master
                    addressUpdateRequest.district?.let { address.district = it }
                    addressUpdateRequest.state?.let { address.state = it }
                }
            }

            // Update other fields if provided (only if pincode was not updated)
            if (addressUpdateRequest.pincode == null) {
                addressUpdateRequest.district?.let { address.district = it }
                addressUpdateRequest.state?.let { address.state = it }
            }

            addressUpdateRequest.addressType?.let { address.addressType = it }
            addressUpdateRequest.isPrimary?.let { address.isPrimary = it }
            addressUpdateRequest.addressOne?.let { address.addressOne = it }
            addressUpdateRequest.addressTwo?.let { address.addressTwo = it }
            addressUpdateRequest.landmark?.let { address.landmark = it }

            val updatedAddress = addressRepository.save(address)
            toAddressResponse(updatedAddress)
        } catch (e: Exception) {
            throw AddressExceptionFactory.updateFailed(messageSource)
        }
    }

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    override fun deleteAddress(entityType: String, entityId: UUID, addressId: UUID) {
        try {
            AddressExceptionFactory.validateAddressForUpdate(addressId, entityType, entityId, messageSource)

            val address = addressRepository.findByEntityTypeAndEntityIdAndId(entityType, entityId, addressId)
                ?: throw AddressExceptionFactory.notFound(addressId, messageSource)

            addressRepository.delete(address)
        } catch (e: Exception) {
            throw AddressExceptionFactory.deleteFailed(messageSource)
        }
    }

    private fun toAddressResponse(address: Address): AddressResponse {
        return AddressResponse(
            id = address.id,
            entityId = address.entityId,
            entityType = address.entityType,
            addressType = address.addressType,
            isPrimary = address.isPrimary,
            addressOne = address.addressOne,
            addressTwo = address.addressTwo,
            landmark = address.landmark,
            district = address.district,
            state = address.state,
            pincode = address.pincode,
            addressSource = address.addressSource,
            extData = address.extData
        )
    }
}
