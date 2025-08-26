package com.nivasafinance.features.person.dto

import com.nivasafinance.features.address.dto.AddressResponse
import java.util.UUID

data class PersonAddressMappingResponse(
    val id: UUID,
    val personId: UUID,
    val address: AddressResponse,
    val addressType: String?
)
