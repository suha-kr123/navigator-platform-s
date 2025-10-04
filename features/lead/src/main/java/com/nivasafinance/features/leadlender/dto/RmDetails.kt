package com.nivasafinance.features.leadlender.dto

import jakarta.validation.constraints.Size

data class RmDetails(
    val name: String? = null,
    @field:Size(min = 10, max = 10, message = "Mobile number must be exactly 10 digits")
    val mobileNumber: String? = null
)
