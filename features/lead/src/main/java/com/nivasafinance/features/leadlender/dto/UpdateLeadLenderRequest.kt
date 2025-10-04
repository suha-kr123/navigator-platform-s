package com.nivasafinance.features.leadlender.dto

import jakarta.validation.constraints.Pattern

data class UpdateLeadLenderRequest(
    val lenderOfficeKey: String? = null,
    val loginId: String? = null,
    val rmName: String? = null,
    @field:Pattern(regexp = "^[0-9]{10}$", message = "Mobile number must be exactly 10 digits")
    val rmMobileNumber: String? = null
)
