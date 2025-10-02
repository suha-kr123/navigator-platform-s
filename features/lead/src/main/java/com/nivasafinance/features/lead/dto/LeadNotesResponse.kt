package com.nivasafinance.features.lead.dto

import com.nivasafinance.features.notes.dto.NotesResponse
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

fun NotesResponse.toLeadNotesResponse(leadId: UUID, taskId: UUID?): LeadNotesResponse {
    return LeadNotesResponse(
        leadId = leadId,
        taskId = taskId,
        noteId = this.id,
        noteTitle = this.title,
        noteContent = this.content,
        noteCreatedAt = this.createdAt,
        noteCreatedBy = this.createdBy ?: "system",
        noteUpdatedAt = this.updatedAt,
        noteUpdatedBy = this.updatedBy ?: "system"
    )
}