package com.nivasafinance.features.offices.dto

import com.nivasafinance.features.address.dto.AddressCreateRequest
import java.util.UUID

data class OfficeCreateRequest(
    val name: String,
    val key: String,
    val addressCreateRequest: AddressCreateRequest,
    val parentId: UUID? = null
)
