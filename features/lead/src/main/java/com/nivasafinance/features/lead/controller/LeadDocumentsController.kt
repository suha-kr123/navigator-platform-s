package com.nivasafinance.features.lead.controller

import base.model.PaginatedResponse
import base.model.PaginationRequest
import com.nivasafinance.features.lead.dto.LeadDocumentCreateRequest
import com.nivasafinance.features.lead.dto.LeadDocumentResponse
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
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@RestController
@RequestMapping("/api/v1/leads/{leadId}/documents")
class LeadDocumentsController(
    private val leadDocumentsService: LeadDocumentsService
) {

    @PostMapping
    fun uploadDocumentToLead(
        @PathVariable leadId: UUID,
        @RequestParam("file") file: MultipartFile,
        @RequestBody createRequest: LeadDocumentCreateRequest
    ): ResponseEntity<LeadDocumentResponse> {
        val leadDocument = leadDocumentsService.createDocumentForLead(leadId, file, createRequest)
        return ResponseEntity.status(HttpStatus.CREATED).body(leadDocument)
    }

    @GetMapping
    fun getDocumentsForLead(
        @PathVariable leadId: UUID,
        @RequestParam(value = "taskId", required = false) taskId: UUID? = null,
        @RequestParam(defaultValue = "0") offset: Int,
        @RequestParam(defaultValue = "20") limit: Int,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "DESC") sortDirection: String
    ): ResponseEntity<PaginatedResponse<LeadDocumentResponse>> {
        val paginationRequest = PaginationRequest(
            offset = offset,
            limit = limit,
            sortBy = sortBy,
            sortDirection = sortDirection
        )
        val documents = if (taskId != null) {
            leadDocumentsService.getDocumentsForLeadTasks(leadId, taskId, paginationRequest)
        } else {
            leadDocumentsService.getDocumentsForLead(leadId, paginationRequest)
        }
        return ResponseEntity.ok(documents)
    }

    @DeleteMapping("{documentId}")
    fun deleteDocumentFromLead(
        @PathVariable leadId: UUID,
        @PathVariable documentId: UUID,
    ): ResponseEntity<Unit> {
        leadDocumentsService.deleteDocumentFromLead(leadId, documentId)
        return ResponseEntity.noContent().build()
    }
}
