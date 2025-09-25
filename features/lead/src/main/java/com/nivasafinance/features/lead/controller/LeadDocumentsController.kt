package com.nivasafinance.features.lead.controller

import base.model.PaginatedResponse
import base.model.PaginationRequest
import com.nivasafinance.features.document.dto.DocumentRequest
import com.nivasafinance.features.lead.dto.LeadDocumentsResponse
import com.nivasafinance.features.lead.service.LeadDocumentsService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/api/leads")
class LeadDocumentsController(
    private val leadDocumentsService: LeadDocumentsService
) {

    // @GetMapping("/documents/all")

    // fun getAllDocuments(
    //     @RequestParam(defaultValue = "0") offset: Int,
    //     @RequestParam(defaultValue = "20") limit: Int,
    //     @RequestParam(defaultValue = "createdAt") sortBy: String,
    //     @RequestParam(defaultValue = "ASC") sortDirection: String
    // ): ResponseEntity<PaginatedResponse<List<LeadDocumentsResponse>>> {
    //     val paginationRequest = PaginationRequest(
    //         offset = offset,
    //         limit = limit,
    //         sortBy = sortBy,
    //         sortDirection = sortDirection
    //     )
    //     val documents = leadDocumentsService.getAllDocuments(paginationRequest)
    //     return ResponseEntity.ok(documents)
    // }

    @PostMapping("/{leadId}/documents")
    fun addDocumentsToLead(
        @PathVariable leadId: UUID,
        @RequestParam taskId: UUID? = null,
        @RequestBody addDocumentsRequest: DocumentRequest
    ): ResponseEntity<LeadDocumentsResponse> {
        val leadDocuments = leadDocumentsService.addDocumentsToLead(leadId, taskId, addDocumentsRequest)
        return ResponseEntity.status(HttpStatus.CREATED).body(leadDocuments)
    }

    @GetMapping("/{leadId}/documents/all")
    fun getTaskDocuments(
        @PathVariable leadId: UUID,
        @RequestParam taskId: UUID? = null,
        @RequestParam(defaultValue = "0") offset: Int,
        @RequestParam(defaultValue = "20") limit: Int,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "ASC") sortDirection: String
    ): ResponseEntity<PaginatedResponse<List<LeadDocumentsResponse>>> {
        val paginationRequest = PaginationRequest(
            offset = offset,
            limit = limit,
            sortBy = sortBy,
            sortDirection = sortDirection
        )

        val taskDocuments = leadDocumentsService.getTaskDocuments(leadId, taskId, paginationRequest)
        return ResponseEntity.ok(taskDocuments)
    }

    @DeleteMapping("/{leadId}/documents/{documentId}")
    fun deleteDocumentById(
        @PathVariable leadId: UUID,
        @PathVariable documentId: UUID,
        @RequestParam taskId: UUID? = null
    ): ResponseEntity<Unit> {
        leadDocumentsService.deleteDocumentById(leadId, taskId, documentId)
        return ResponseEntity.noContent().build()
    }
}

