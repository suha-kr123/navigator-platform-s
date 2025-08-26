package com.nivasafinance.features.person.dto

import annotations.NoArg
import com.nivasafinance.features.person.enum.IdentifierType
import java.util.UUID

@NoArg
data class PersonIdentifierMappingResponse(
    val id: UUID,
    val personId: UUID,
    val identifierId: UUID,
    val type: IdentifierType,
    val isPrimary: Boolean
)
