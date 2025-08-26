package com.nivasafinance.features.person.dto

import com.nivasafinance.features.person.enum.IdentifierType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class PersonIdentifierCreateRequest(
    @field:NotBlank(message = "Identifier is required")
    val identifier: String,

    @field:NotNull(message = "Identifier type is required")
    val type: IdentifierType
)
