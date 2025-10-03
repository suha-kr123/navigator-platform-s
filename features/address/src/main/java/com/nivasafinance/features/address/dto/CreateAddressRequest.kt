package com.nivasafinance.features.address.dto

import com.nivasafinance.features.address.enum.AddressSource
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class CreateAddressRequest(
    val addressOne: String? = null,
    val addressTwo: String? = null,
    val landmark: String? = null,
    val district: String? = null,
    val state: String? = null,

    @field:NotBlank(message = "Pincode is mandatory")
    @field:Pattern(regexp = "^[0-9]{6}$", message = "Pincode must be exactly 6 digits")
    val pincode: String,

    val addressSource: AddressSource
)
