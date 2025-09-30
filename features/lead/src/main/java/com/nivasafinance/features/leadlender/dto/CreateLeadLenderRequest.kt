package com.nivasafinance.features.leadlender.dto

import jakarta.validation.constraints.NotBlank

data class CreateLeadLenderRequest(
    @field:NotBlank(message = "Lender key is required")
    val lenderKey: String
)
