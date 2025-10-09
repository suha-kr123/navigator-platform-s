package com.nivasafinance.features.notes.dto

import java.time.LocalDateTime
import java.util.UUID

data class NotesResponse(
    val id: UUID,
    val title: String,
    val content: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val createdBy: String?,
    val updatedBy: String?
)
