package com.nivasafinance.features.notes.dto

import com.nivasafinance.features.notes.enum.EntityType

import java.util.UUID

data class NotesRequest(
    val notes: String,
    val entityType: EntityType,
    val entityId: UUID
)