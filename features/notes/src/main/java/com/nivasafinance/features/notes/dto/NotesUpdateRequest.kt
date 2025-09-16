package com.nivasafinance.features.notes.dto

import java.util.UUID
import com.nivasafinance.features.notes.enum.EntityType

data class NotesUpdateRequest(
    val notes: String,
    val entityType: EntityType,
    val entityId: UUID
)

