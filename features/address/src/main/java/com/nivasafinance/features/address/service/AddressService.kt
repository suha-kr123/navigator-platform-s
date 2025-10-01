package com.nivasafinance.features.address.service

import com.nivasafinance.features.address.dto.AddressResponse
import com.nivasafinance.features.address.dto.CreateAddressRequest
import com.nivasafinance.features.address.dto.UpdateAddressRequest
import java.util.UUID

interface AddressService {
    fun getAllAddresses(): List<AddressResponse>
    fun getAddressById(addressId: UUID): AddressResponse?
    fun createAddress(addressRequest: CreateAddressRequest): AddressResponse
    fun updateAddress(addressId: UUID, updateAddressRequest: UpdateAddressRequest): AddressResponse
    fun deleteAddress(addressId: UUID)
}
