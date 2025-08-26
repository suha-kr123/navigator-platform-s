package com.nivasafinance.features.person.dto

import annotations.NoArg
import jakarta.validation.constraints.Pattern

@NoArg
data class PersonAddressMappingUpdateRequest(
    // Address update fields
    var addressOne: String? = null,
    var addressTwo: String? = null,
    var landmark: String? = null,
    var district: String? = null,
    var state: String? = null,
    @field:Pattern(regexp = "^[0-9]{6}$", message = "Pincode must be exactly 6 digits")
    var pincode: String? = null,
    var addressSource: String? = null,

    // Mapping update field
    var addressType: String? = null // HOME, OFFICE, PERMANENT, etc.
)
