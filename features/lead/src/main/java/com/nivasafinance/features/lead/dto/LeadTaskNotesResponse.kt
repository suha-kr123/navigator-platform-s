package com.nivasafinance.features.lead.dto

import com.nivasafinance.features.notes.dto.NotesResponse
import java.util.*

data class LeadTaskNotesResponse(
    val leadId: UUID,
    val taskId: UUID,
    val notes: List<NotesResponse>
)
