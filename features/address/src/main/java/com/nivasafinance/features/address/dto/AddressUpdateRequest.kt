package com.nivasafinance.features.address.dto


data class AddressUpdateRequest(
    val addressType: String? = null,
    val isPrimary: Boolean? = null,
    val addressOne: String? = null,
    val addressTwo: String? = null,
    val landmark: String? = null,
    val district: String? = null,
    val state: String? = null,
    val pincode: String? = null
)
