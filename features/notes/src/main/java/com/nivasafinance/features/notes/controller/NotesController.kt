package com.nivasafinance.features.notes.controller

import base.model.PaginatedResponse
import base.model.PaginationRequest
import base.model.SortDirection
import base.model.SortDirection.ASC
import base.model.SortDirection.DESC
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

    @GetMapping("/{entityType}/{entityId}")
    fun getNotesByEntity(
            @PathVariable entityType: String,
            @PathVariable entityId: UUID,
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
                        sortDirection =
                                if (sortDirection == "ASC") SortDirection.ASC
                                else SortDirection.DESC
                )
        val notes = notesService.getNotesByEntity(entityType, entityId, paginationRequest)
        return ResponseEntity.ok(notes)
    }

    @PostMapping("/{entityType}/{entityId}")
    fun createNotes(
            @PathVariable entityType: String,
            @PathVariable entityId: UUID,
            @RequestBody notesRequest: NotesRequest
    ): ResponseEntity<NotesResponse> {
        val createdNotes = notesService.createNotesByEntity(entityType, entityId, notesRequest)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdNotes)
    }

    @PutMapping("/{entityType}/{entityId}/{notesId}")
    fun updateNotes(
            @PathVariable entityType: String,
            @PathVariable entityId: UUID,
            @PathVariable notesId: UUID,
            @RequestBody notesUpdateRequest: NotesUpdateRequest
    ): ResponseEntity<NotesResponse> {
        val updatedNotes =
                notesService.updateNotesByEntity(entityType, entityId, notesId, notesUpdateRequest)
        return ResponseEntity.ok(updatedNotes)
    }

    @DeleteMapping("/{entityType}/{entityId}/{notesId}")
    fun deleteNotes(
            @PathVariable entityType: String,
            @PathVariable entityId: UUID,
            @PathVariable notesId: UUID
    ): ResponseEntity<Void> {
        notesService.deleteNotesByEntity(entityType, entityId, notesId)
        return ResponseEntity.noContent().build()
    }
}
