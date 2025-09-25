package com.nivasafinance.features.lead.service

import base.model.PaginatedResponse
import base.model.PaginationRequest
import com.nivasafinance.features.document.dto.DocumentRequest
import com.nivasafinance.features.document.dto.DocumentVerificationRequest
import com.nivasafinance.features.lead.dto.LeadTaskDocumentsResponse
import java.util.UUID

interface LeadDocumentsService {
    fun addDocumentsToLead(
        leadId: UUID,
        taskId: UUID,
        addDocumentsToLeadRequest: DocumentRequest
    ): LeadTaskDocumentsResponse
    fun getDocumentById(leadId: UUID, taskId: UUID, documentId: UUID): LeadTaskDocumentsResponse
    fun getLeadDocuments(
        leadId: UUID,
        paginationRequest: PaginationRequest
    ): PaginatedResponse<LeadTaskDocumentsResponse>
    fun getTaskDocuments(
        leadId: UUID,
        taskId: UUID,
        paginationRequest: PaginationRequest
    ): PaginatedResponse<LeadTaskDocumentsResponse>
    fun getAllLeadsDocuments(paginationRequest: PaginationRequest): PaginatedResponse<LeadTaskDocumentsResponse>
    fun verifyDocument(
        leadId: UUID,
        taskId: UUID,
        documentId: UUID,
        verifyDocumentRequest: DocumentVerificationRequest
    ): LeadTaskDocumentsResponse
    fun deleteDocumentById(leadId: UUID, taskId: UUID, documentId: UUID)
}
