package com.nivasafinance.features.address.service

import com.nivasafinance.features.address.dto.AddressResponse
import java.util.UUID

interface AddressReadService {
    fun getAddress(id: UUID): AddressResponse
    fun getAddresses(ids: List<UUID>): List<AddressResponse>
}
