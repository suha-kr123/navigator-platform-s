package com.nivasafinance.features.identifiers.dto


data class IdentifierRequest(
    val identifier: String,
    val type: String,
    val verificationStatus: String? = null,
    val verificationNotes: String? = null,
    val extData: Map<String, Any>? = null
)
