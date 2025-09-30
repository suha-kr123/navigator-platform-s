package com.nivasafinance.features.lead.service

import base.model.PaginatedResponse
import base.model.PaginationRequest
import com.nivasafinance.features.document.dto.DocumentRequest
import com.nivasafinance.features.lead.dto.LeadDocumentsResponse
import java.util.UUID

interface LeadDocumentsService {
    fun addDocumentsToLead(
        leadId: UUID,
        taskId: UUID?,
        addDocumentsToLeadRequest: DocumentRequest
    ): LeadDocumentsResponse
    fun getTaskDocuments(
        leadId: UUID,
        taskId: UUID?,
        paginationRequest: PaginationRequest
    ): PaginatedResponse<List<LeadDocumentsResponse>>
    fun deleteDocumentById(leadId: UUID, taskId: UUID?, documentId: UUID)
    fun getAllDocuments(paginationRequest: PaginationRequest): PaginatedResponse<List<LeadDocumentsResponse>>
}
