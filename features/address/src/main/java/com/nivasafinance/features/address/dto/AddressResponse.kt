package com.nivasafinance.features.address.dto

import annotations.NoArg
import java.util.UUID

@NoArg
data class AddressResponse(
    val id: UUID? = null,
    val entityId: UUID?,
    val entityType: String?,
    val addressType: String?,
    val isPrimary: Boolean,
    var addressOne: String? = null,
    var addressTwo: String? = null,
    var landmark: String? = null,
    var district: String? = null,
    var state: String? = null,
    var pincode: String,
    var addressSource: String? = null
)
