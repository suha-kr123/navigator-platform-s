package com.nivasafinance.features.address.service

import com.nivasafinance.features.address.dto.AddressCreateRequest
import com.nivasafinance.features.address.dto.AddressResponse
import com.nivasafinance.features.address.dto.AddressUpdateRequest
import com.nivasafinance.features.address.enum.AddressType
import java.util.UUID

interface AddressService {
    fun getAddressesByEntityTypeAndEntityId(entityType: String, entityId: UUID): List<AddressResponse>
    fun getAddressByEntityTypeAndEntityIdAndAddressType(
        entityType: String,
        entityId: UUID,
        addressType: AddressType
    ): AddressResponse?
    fun getAddressByEntityTypeAndEntityIdAndId(
        entityType: String,
        entityId: UUID,
        addressId: UUID
    ): AddressResponse?
    fun createAddress(entityType: String, entityId: UUID, addressRequest: AddressCreateRequest): AddressResponse
    fun updateAddress(
        entityType: String,
        entityId: UUID,
        addressId: UUID,
        addressUpdateRequest: AddressUpdateRequest
    ): AddressResponse
    fun deleteAddress(entityType: String, entityId: UUID, addressId: UUID)
}
