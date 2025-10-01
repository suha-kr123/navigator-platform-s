package com.nivasafinance.features.offices.dto

import com.nivasafinance.features.address.dto.CreateAddressRequest
import java.util.UUID

data class OfficeCreateRequest(
    val name: String,
    val key: String,
    val createAddressRequest: CreateAddressRequest,
    val parentId: UUID? = null
)
