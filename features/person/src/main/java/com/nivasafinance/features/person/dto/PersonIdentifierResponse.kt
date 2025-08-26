package com.nivasafinance.features.person.dto

import com.nivasafinance.features.person.enum.IdentifierType
import java.util.UUID

data class PersonIdentifierResponse(
    val id: UUID,
    val personId: UUID,
    val identifier: String,
    val type: IdentifierType
)
