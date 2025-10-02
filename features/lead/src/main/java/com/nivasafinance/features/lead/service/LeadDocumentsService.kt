package com.nivasafinance.features.lead.service

import base.model.PaginatedResponse
import base.model.PaginationRequest
import com.nivasafinance.features.lead.dto.LeadDocumentCreateRequest
import com.nivasafinance.features.lead.dto.LeadDocumentResponse
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

interface LeadDocumentsService {
    /**
     * Creates a document for a specific lead using composition with DocumentService
     */
    fun createDocumentForLead(
        leadId: UUID,
        file: MultipartFile,
        createRequest: LeadDocumentCreateRequest
    ): LeadDocumentResponse

    /**
     * Gets all documents for a specific lead
     */
    fun getDocumentsForLead(
        leadId: UUID,
        paginationRequest: PaginationRequest
    ): PaginatedResponse<LeadDocumentResponse>

    fun getDocumentsForLeadTasks(
        leadId: UUID,
        taskId: UUID,
        paginationRequest: PaginationRequest
    ): PaginatedResponse<LeadDocumentResponse>

    /**
     * Deletes a document from a lead
     */
    fun deleteDocumentFromLead(leadId: UUID, documentId: UUID)
}
