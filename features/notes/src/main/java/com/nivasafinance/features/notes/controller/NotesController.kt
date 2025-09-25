package com.nivasafinance.features.notes.controller

import base.model.PaginatedResponse
import base.model.PaginationRequest
import com.nivasafinance.features.notes.dto.NotesRequest
import com.nivasafinance.features.notes.dto.NotesResponse
import com.nivasafinance.features.notes.dto.NotesUpdateRequest
import com.nivasafinance.features.notes.service.NotesService
import java.util.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/notes")
class NotesController(private val notesService: NotesService) {

    @GetMapping
    fun getAllNotes(
            @RequestParam(defaultValue = "0") offset: Int,
            @RequestParam(defaultValue = "20") limit: Int,
            @RequestParam(required = false) sortBy: String?,
            @RequestParam(defaultValue = "DESC") sortDirection: String
    ): ResponseEntity<PaginatedResponse<NotesResponse>> {
        val paginationRequest =
                PaginationRequest(
                        offset = offset,
                        limit = limit,
                        sortBy = sortBy,
                        sortDirection = sortDirection
                )
        val notes = notesService.getAllNotes(paginationRequest)
        return ResponseEntity.ok(notes)
    }

    @GetMapping("/{notesId}")
    fun getNotesById(@PathVariable notesId: UUID): ResponseEntity<NotesResponse> {
        val notes = notesService.getNotesById(notesId)
        return ResponseEntity.ok(notes)
    }

    @PostMapping
    fun createNotes(@RequestBody notesRequest: NotesRequest): ResponseEntity<NotesResponse> {
        val createdNotes = notesService.createNotes(notesRequest)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdNotes)
    }

    @PutMapping("/{notesId}")
    fun updateNotes(
            @PathVariable notesId: UUID,
            @RequestBody notesUpdateRequest: NotesUpdateRequest
    ): ResponseEntity<NotesResponse> {
        val updatedNotes = notesService.updateNotes(notesId, notesUpdateRequest)
        return ResponseEntity.ok(updatedNotes)
    }

    @PatchMapping("/{notesId}")
    fun patchNotes(
            @PathVariable notesId: UUID,
            @RequestBody notesUpdateRequest: NotesUpdateRequest
    ): ResponseEntity<NotesResponse> {
        val updatedNotes = notesService.patchNotes(notesId, notesUpdateRequest)
        return ResponseEntity.ok(updatedNotes)
    }

    @DeleteMapping("/{notesId}")
    fun deleteNotes(@PathVariable notesId: UUID): ResponseEntity<Void> {
        notesService.deleteNotes(notesId)
        return ResponseEntity.noContent().build()
    }
}
