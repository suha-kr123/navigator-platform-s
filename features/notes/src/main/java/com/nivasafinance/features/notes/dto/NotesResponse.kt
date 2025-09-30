package com.nivasafinance.features.notes.dto

import java.util.UUID
import java.time.LocalDateTime

data class NotesResponse(
    val id: UUID,
    val title: String,
    val content: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val createdBy: String?,
    val updatedBy: String?
)