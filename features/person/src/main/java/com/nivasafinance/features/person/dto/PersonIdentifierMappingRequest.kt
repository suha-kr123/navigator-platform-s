package com.nivasafinance.features.person.dto

import annotations.NoArg
import jakarta.validation.constraints.NotNull
import java.util.UUID

@NoArg
data class PersonIdentifierMappingRequest(
    @field:NotNull
    val identifierId: UUID,

    val isPrimary: Boolean = false
)
