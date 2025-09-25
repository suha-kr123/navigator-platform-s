package com.nivasafinance.features.lead.controller

import base.model.PaginatedResponse
import base.model.PaginationRequest
import com.nivasafinance.features.document.dto.DocumentRequest
import com.nivasafinance.features.lead.dto.LeadTaskDocumentsResponse
import com.nivasafinance.features.lead.service.LeadDocumentsService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/leads")
class LeadTaskDocumentsController(
    private val leadDocumentsService: LeadDocumentsService
) {

    @GetMapping("/all/documents")
    fun getAllDocuments(
        @RequestParam(defaultValue = "0") offset: Int,
        @RequestParam(defaultValue = "20") limit: Int,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "ASC") sortDirection: String
    ): ResponseEntity<PaginatedResponse<List<LeadTaskDocumentsResponse>>> {
        val paginationRequest = PaginationRequest(
            offset = offset,
            limit = limit,
            sortBy = sortBy,
            sortDirection = sortDirection
        )
        val leadDocuments = leadDocumentsService.getAllLeadsDocuments(paginationRequest)
        return ResponseEntity.ok(leadDocuments)
    }

    @GetMapping("/{leadId}/documents")
    fun getLeadDocuments(
        @PathVariable leadId: UUID,
        @RequestParam(defaultValue = "0") offset: Int,
        @RequestParam(defaultValue = "20") limit: Int,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "ASC") sortDirection: String
    ): ResponseEntity<PaginatedResponse<List<LeadTaskDocumentsResponse>>> {
        val paginationRequest = PaginationRequest(
            offset = offset,
            limit = limit,
            sortBy = sortBy,
            sortDirection = sortDirection
        )
        val leadDocuments = leadDocumentsService.getLeadDocuments(leadId, paginationRequest)
        return ResponseEntity.ok(leadDocuments)
    }

    @PostMapping("/{leadId}/tasks/{taskId}/documents")
    fun addDocumentsToTask(
        @PathVariable leadId: UUID,
        @PathVariable taskId: UUID,
        @RequestBody addDocumentsRequest: DocumentRequest
    ): ResponseEntity<LeadTaskDocumentsResponse> {
        val leadDocuments = leadDocumentsService.addDocumentsToLead(leadId, taskId, addDocumentsRequest)
        return ResponseEntity.status(HttpStatus.CREATED).body(leadDocuments)
    }

    @GetMapping("/{leadId}/tasks/{taskId}/documents/{documentId}")
    fun getDocumentById(
        @PathVariable leadId: UUID,
        @PathVariable taskId: UUID,
        @PathVariable documentId: UUID
    ): ResponseEntity<LeadTaskDocumentsResponse> {
        val document = leadDocumentsService.getDocumentById(leadId, taskId, documentId)
        return ResponseEntity.ok(document)
    }

    @GetMapping("/{leadId}/tasks/{taskId}/documents")
    fun getTaskDocuments(
        @PathVariable leadId: UUID,
        @PathVariable taskId: UUID,
        @RequestParam(defaultValue = "0") offset: Int,
        @RequestParam(defaultValue = "20") limit: Int,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "ASC") sortDirection: String
    ): ResponseEntity<PaginatedResponse<List<LeadTaskDocumentsResponse>>> {
        val paginationRequest = PaginationRequest(
            offset = offset,
            limit = limit,
            sortBy = sortBy,
            sortDirection = sortDirection
        )

        val taskDocuments = leadDocumentsService.getTaskDocuments(leadId, taskId, paginationRequest)
        return ResponseEntity.ok(taskDocuments)
    }

    @DeleteMapping("/{leadId}/tasks/{taskId}/documents/{documentId}")
    fun deleteDocumentById(
        @PathVariable leadId: UUID,
        @PathVariable taskId: UUID,
        @PathVariable documentId: UUID
    ): ResponseEntity<Unit> {
        leadDocumentsService.deleteDocumentById(leadId, taskId, documentId)
        return ResponseEntity.noContent().build()
    }
}
