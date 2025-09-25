package com.nivasafinance.features.lead.dto

import com.nivasafinance.features.notes.dto.NotesResponse
import java.util.*
import java.time.LocalDateTime

data class LeadTaskNotesResponse(
    val leadId: UUID,
    val taskId: UUID,
    val noteId: UUID,
    val noteTitle: String,
    val noteContent: String,
    val noteCreatedAt: LocalDateTime,
    val noteCreatedBy: String,
    val noteUpdatedAt: LocalDateTime,
    val noteUpdatedBy: String
)
