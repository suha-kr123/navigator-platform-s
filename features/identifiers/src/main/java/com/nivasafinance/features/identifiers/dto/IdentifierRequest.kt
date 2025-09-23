package com.nivasafinance.features.identifiers.dto

import com.nivasafinance.features.identifiers.enum.IdentifierType

data class IdentifierRequest(
    val identifier: String,
    val type: IdentifierType,
    val verificationStatus: String? = null,
    val verificationNotes: String? = null,
    val extData: Map<String, Any>? = null
)
