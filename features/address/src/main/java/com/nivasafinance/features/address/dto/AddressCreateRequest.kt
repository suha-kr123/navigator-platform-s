package com.nivasafinance.features.address.dto

import annotations.NoArg
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import java.util.UUID

@NoArg
data class AddressCreateRequest(
    val id: UUID? = null,
    var addressOne: String? = null,
    var addressTwo: String? = null,
    var landmark: String? = null,
    var district: String? = null,
    var state: String? = null,
    @field:NotBlank(message = "Pincode is mandatory")
    @field:Pattern(regexp = "^[0-9]{6}$", message = "Pincode must be exactly 6 digits")
    var pincode: String,
    var addressSource: String? = null
)
