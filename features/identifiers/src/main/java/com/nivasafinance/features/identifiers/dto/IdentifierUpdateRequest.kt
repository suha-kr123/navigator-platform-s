package com.nivasafinance.features.identifiers.dto

import com.nivasafinance.features.identifiers.enum.IdentifierType

data class IdentifierUpdateRequest(
    val identifier: String? = null,
    val type: IdentifierType? = null,
    val verificationStatus: String? = null,
    val verificationNotes: String? = null,
    val extData: Map<String, Any>? = null
)
