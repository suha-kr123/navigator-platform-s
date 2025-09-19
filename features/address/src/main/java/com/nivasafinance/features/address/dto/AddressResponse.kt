package com.nivasafinance.features.address.dto

import com.nivasafinance.features.address.enum.AddressSource
import com.nivasafinance.features.address.enum.AddressType
import java.util.UUID

data class AddressResponse(
    val id: UUID?,
    val entityId: UUID?,
    val entityType: String?,
    val addressType: AddressType?,
    val isPrimary: Boolean,
    val addressOne: String?,
    val addressTwo: String?,
    val landmark: String?,
    val district: String?,
    val state: String?,
    val pincode: String,
    val addressSource: AddressSource?,
    val extData: Map<String, Any>?
)
