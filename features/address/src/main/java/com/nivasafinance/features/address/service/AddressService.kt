package com.nivasafinance.features.address.service

import com.nivasafinance.features.address.dto.AddressCreateRequest
import com.nivasafinance.features.address.dto.AddressResponse
import com.nivasafinance.features.address.dto.AddressUpdateRequest
import java.util.UUID

interface AddressService {

    fun getAddress(id: UUID): AddressResponse

    fun createAddress(request: AddressCreateRequest): AddressResponse

    fun updateAddress(id: UUID, request: AddressUpdateRequest): AddressResponse

    fun deleteAddress(id: UUID)
}
