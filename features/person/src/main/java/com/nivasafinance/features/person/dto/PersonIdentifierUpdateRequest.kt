package com.nivasafinance.features.person.dto

import com.nivasafinance.features.person.enum.IdentifierType

data class PersonIdentifierUpdateRequest(
    val identifier: String?,
    val type: IdentifierType?
)
