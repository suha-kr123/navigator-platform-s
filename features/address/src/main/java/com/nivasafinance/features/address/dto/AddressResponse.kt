package com.nivasafinance.features.address.dto

import java.util.UUID

data class AddressResponse(
    val id: UUID?,
    val addressOne: String?,
    val addressTwo: String?,
    val landmark: String?,
    val district: String?,
    val state: String?,
    val pincode: String,
    val addressSource: String?,
    val extData: Map<String, Any>?
)
