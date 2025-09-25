package com.nivasafinance.features.lead.dto

import java.time.LocalDateTime
import java.util.UUID

data class LeadNotesResponse(
    val leadId: UUID,
    val taskId: UUID? = null,
    val noteId: UUID,
    val noteTitle: String,
    val noteContent: String,
    val noteCreatedAt: LocalDateTime,
    val noteCreatedBy: String,
    val noteUpdatedAt: LocalDateTime,
    val noteUpdatedBy: String
)
