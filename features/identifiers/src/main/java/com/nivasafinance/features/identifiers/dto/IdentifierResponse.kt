package com.nivasafinance.features.identifiers.dto

import com.nivasafinance.features.identifiers.enum.EntityType
import com.nivasafinance.features.identifiers.enum.IdentifierType
import java.time.LocalDateTime
import java.util.UUID

data class IdentifierResponse(
    val id: UUID,
    val entityId: UUID,
    val entityType: EntityType,
    val identifier: String,
    val type: IdentifierType,
    val verificationStatus: String?,
    val verificationNotes: String?,
    val extData: Map<String, Any>?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val createdBy: String?,
    val updatedBy: String?
)
