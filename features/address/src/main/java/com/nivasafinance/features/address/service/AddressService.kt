package com.nivasafinance.features.address.service

import com.nivasafinance.features.address.dto.AddressCreateRequest
import com.nivasafinance.features.address.dto.AddressResponse
import com.nivasafinance.features.address.dto.AddressUpdateRequest
import java.util.UUID

interface AddressService {
    fun getAllAddresses(): List<AddressResponse>
    fun getAddressById(addressId: UUID): AddressResponse?
    fun getAddressesByAddressType(addressType: String): List<AddressResponse>
    fun createAddress(addressRequest: AddressCreateRequest): AddressResponse
    fun updateAddress(addressId: UUID, addressUpdateRequest: AddressUpdateRequest): AddressResponse
    fun deleteAddress(addressId: UUID)
}
