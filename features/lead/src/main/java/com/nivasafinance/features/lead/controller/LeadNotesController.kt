package com.nivasafinance.features.lead.controller

import base.model.PaginatedResponse
import base.model.PaginationRequest
import com.nivasafinance.features.lead.dto.LeadNotesResponse
import com.nivasafinance.features.lead.service.LeadNotesService
import com.nivasafinance.features.notes.dto.NotesRequest
import com.nivasafinance.features.notes.dto.NotesUpdateRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/leads")
class LeadNotesController(
    private val leadNotesService: LeadNotesService
) {

    // @GetMapping("/notes/all")
    // fun getAllNotes(
    //     @RequestParam(defaultValue = "0") offset: Int,
    //     @RequestParam(defaultValue = "20") limit: Int,
    //     @RequestParam(defaultValue = "createdAt") sortBy: String,
    //     @RequestParam(defaultValue = "ASC") sortDirection: String
    // ): ResponseEntity<PaginatedResponse<List<LeadNotesResponse>>> {
    //     val paginationRequest = PaginationRequest(
    //         offset = offset,
    //         limit = limit,
    //         sortBy = sortBy,
    //         sortDirection = sortDirection
    //     )
    //     val notes = leadNotesService.getAllNotes(paginationRequest)
    //     return ResponseEntity.ok(notes)
    // }

    @PostMapping("/{leadId}/notes")
    fun addNotesToLead(
        @PathVariable leadId: UUID,
        @RequestParam taskId: UUID? = null,
        @RequestBody addNotesRequest: NotesRequest
    ): ResponseEntity<LeadNotesResponse> {
        val leadNotes = leadNotesService.addNotesToLead(leadId, taskId, addNotesRequest)
        return ResponseEntity.status(HttpStatus.CREATED).body(leadNotes)
    }

    @GetMapping("/{leadId}/notes/all")
    fun getNotesByLeadId(
        @PathVariable leadId: UUID,
        @RequestParam taskId: UUID? = null,
        @RequestParam(defaultValue = "0") offset: Int,
        @RequestParam(defaultValue = "20") limit: Int,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "ASC") sortDirection: String
    ): ResponseEntity<PaginatedResponse<List<LeadNotesResponse>>> {
        val paginationRequest = PaginationRequest(
            offset = offset,
            limit = limit,
            sortBy = sortBy,
            sortDirection = sortDirection
        )

        val leadNotes = leadNotesService.getTaskNotes(leadId, taskId, paginationRequest)
        return ResponseEntity.ok(leadNotes)
    }

    @PatchMapping("/{leadId}/notes/{notesId}")
    fun patchNotesByLeadId(
        @PathVariable leadId: UUID,
        @RequestParam taskId: UUID? = null,
        @PathVariable notesId: UUID,
        @RequestBody updateNotesRequest: NotesUpdateRequest
    ): ResponseEntity<LeadNotesResponse> {
        val updatedNotes = leadNotesService.patchNotesById(leadId, taskId, notesId, updateNotesRequest)
        return ResponseEntity.ok(updatedNotes)
    }

    @DeleteMapping("/{leadId}/notes/{notesId}")
    fun deleteNotesByLeadId(
        @PathVariable leadId: UUID,
        @RequestParam taskId: UUID? = null,
        @PathVariable notesId: UUID
    ): ResponseEntity<Unit> {
        leadNotesService.deleteNotesById(leadId, taskId, notesId)
        return ResponseEntity.noContent().build()
    }
}
