package com.nivasafinance.features.identifiers.dto

import java.time.LocalDateTime
import java.util.UUID

data class IdentifierResponse(
    val id: UUID,
    val identifier: String,
    val type: String,
    val extData: Map<String, Any>?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val createdBy: String?,
    val updatedBy: String?
)
