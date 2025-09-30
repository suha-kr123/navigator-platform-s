package com.nivasafinance.features.address.service.impl

import com.nivasafinance.features.address.dto.AddressCreateRequest
import com.nivasafinance.features.address.dto.AddressResponse
import com.nivasafinance.features.address.dto.AddressUpdateRequest
import com.nivasafinance.features.address.entity.Address
import com.nivasafinance.features.address.exception.AddressExceptionFactory
import com.nivasafinance.features.address.repository.AddressRepositoryWrapper
import com.nivasafinance.features.address.service.AddressService
import com.nivasafinance.features.master.pincode.service.PincodeService
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class AddressServiceImpl(
    private val addressRepositoryWrapper: AddressRepositoryWrapper,
    private val pincodeService: PincodeService,
    private val messageSource: MessageSource
) : AddressService {

    override fun getAllAddresses(): List<AddressResponse> {
        val addresses = addressRepositoryWrapper.findAllWithException()
        return addresses.map { toAddressResponse(it) }
    }

    override fun getAddressById(addressId: UUID): AddressResponse? {
        return try {
            val address = addressRepositoryWrapper.findByIdWithException(addressId)
            toAddressResponse(address)
        } catch (e: Exception) {
            null
        }
    }

    override fun createAddress(addressRequest: AddressCreateRequest): AddressResponse {
        AddressExceptionFactory.validateAddressForCreation(addressRequest.pincode, messageSource)

        val pincodeDetails = pincodeService.getPincodeDetailsSafe(addressRequest.pincode)

        val (finalDistrict, finalState) = if (pincodeDetails.isNotEmpty()) {
            val masterData = pincodeDetails.first()
            Pair(masterData.district ?: addressRequest.district, masterData.state ?: addressRequest.state)
        } else {
            Pair(addressRequest.district, addressRequest.state)
        }

        val address = Address(
            addressType = addressRequest.addressType,
            addressOne = addressRequest.addressOne,
            addressTwo = addressRequest.addressTwo,
            landmark = addressRequest.landmark,
            district = finalDistrict,
            state = finalState,
            pincode = addressRequest.pincode,
            addressSource = addressRequest.addressSource
        )

        val savedAddress = addressRepositoryWrapper.saveWithException(address)
        return toAddressResponse(savedAddress)
    }

    override fun updateAddress(addressId: UUID, addressUpdateRequest: AddressUpdateRequest): AddressResponse {
        AddressExceptionFactory.validateAddressForUpdate(addressId, messageSource)

        val address = addressRepositoryWrapper.findByIdWithException(addressId)

        addressUpdateRequest.pincode?.let { pincode ->
            AddressExceptionFactory.validateAddressForCreation(pincode, messageSource)

            val pincodeDetails = pincodeService.getPincodeDetailsSafe(pincode)

            address.pincode = pincode

            if (pincodeDetails.isNotEmpty()) {
                val masterData = pincodeDetails.first()
                masterData.district?.let { address.district = it }
                masterData.state?.let { address.state = it }
            } else {
                addressUpdateRequest.district?.let { address.district = it }
                addressUpdateRequest.state?.let { address.state = it }
            }
        }

        if (addressUpdateRequest.pincode == null) {
            addressUpdateRequest.district?.let { address.district = it }
            addressUpdateRequest.state?.let { address.state = it }
        }

        addressUpdateRequest.addressType?.let { address.addressType = it }
        addressUpdateRequest.addressOne?.let { address.addressOne = it }
        addressUpdateRequest.addressTwo?.let { address.addressTwo = it }
        addressUpdateRequest.landmark?.let { address.landmark = it }

        val updatedAddress = addressRepositoryWrapper.saveWithException(address)
        return toAddressResponse(updatedAddress)
    }

    override fun deleteAddress(addressId: UUID) {
        addressRepositoryWrapper.deleteByIdWithException(addressId)
    }

    private fun toAddressResponse(address: Address): AddressResponse {
        return AddressResponse(
            id = address.id,
            addressType = address.addressType,
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
