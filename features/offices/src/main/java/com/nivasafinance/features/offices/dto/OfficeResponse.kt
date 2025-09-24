package com.nivasafinance.features.offices.dto

import com.nivasafinance.features.address.dto.AddressResponse
import java.util.UUID

data class OfficeResponse(
    val id: UUID,
    val name: String,
    val key: String,
    val code: String,
    val address: AddressResponse?
)
