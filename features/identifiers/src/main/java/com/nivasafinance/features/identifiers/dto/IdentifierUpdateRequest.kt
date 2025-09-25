package com.nivasafinance.features.identifiers.dto


data class IdentifierUpdateRequest(
    val identifier: String? = null,
    val type: String? = null,
    val verificationStatus: String? = null,
    val verificationNotes: String? = null,
    val extData: Map<String, Any>? = null
)
