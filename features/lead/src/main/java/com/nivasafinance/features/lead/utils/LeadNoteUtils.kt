package com.nivasafinance.features.lead.utils

import com.nivasafinance.features.lead.dto.LeadNotesResponse
import com.nivasafinance.features.lead.dto.toLeadNotesResponse
import com.nivasafinance.features.lead.entity.LeadNotesData
import com.nivasafinance.features.notes.service.NotesService
import java.util.UUID

fun LeadNotesData.toLeadNoteResponse(leadId: UUID, notesService: NotesService): LeadNotesResponse {
    val note = notesService.getNotesById(noteId)
    return note.toLeadNotesResponse(leadId = leadId, taskId = taskId)
}
