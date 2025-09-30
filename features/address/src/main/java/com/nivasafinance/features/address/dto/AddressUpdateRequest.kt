package com.nivasafinance.features.address.dto

import jakarta.validation.constraints.Pattern

data class AddressUpdateRequest(
    val addressType: String? = null,
    val addressOne: String? = null,
    val addressTwo: String? = null,
    val landmark: String? = null,
    val district: String? = null,
    val state: String? = null,
    
    @field:Pattern(regexp = "^[0-9]{6}$", message = "Pincode must be exactly 6 digits")
    val pincode: String? = null
)
