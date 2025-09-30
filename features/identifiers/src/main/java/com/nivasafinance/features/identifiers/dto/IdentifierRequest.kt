package com.nivasafinance.features.identifiers.dto

data class IdentifierRequest(
    val identifier: String,
    val type: String,
    val extData: Map<String, Any>? = null
)
