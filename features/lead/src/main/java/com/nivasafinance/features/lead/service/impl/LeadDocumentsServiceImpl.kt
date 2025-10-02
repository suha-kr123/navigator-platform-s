package com.nivasafinance.features.lead.service.impl

import base.model.PaginatedResponse
import base.model.PaginationInfo
import base.model.PaginationRequest
import base.model.toBasicPaginatedResponse
import com.nivasafinance.features.document.dto.DocumentCreateRequest
import com.nivasafinance.features.document.service.DocumentReadService
import com.nivasafinance.features.document.service.DocumentWriteService
import com.nivasafinance.features.lead.dto.LeadDocumentCreateRequest
import com.nivasafinance.features.lead.dto.LeadDocumentCreateResponse
import com.nivasafinance.features.lead.dto.LeadDocumentResponse
import com.nivasafinance.features.lead.entity.Lead
import com.nivasafinance.features.lead.entity.LeadDocumentData
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper
import com.nivasafinance.features.lead.service.LeadDocumentsService
import com.nivasafinance.features.lead.utils.resolveLeadDocumentPath
import com.nivasafinance.features.lead.utils.toLeadDocumentResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@Service
@Transactional
class LeadDocumentsServiceImpl(
    private val documentWriteService: DocumentWriteService,
    private val documentReadService: DocumentReadService,
    private val leadRepositoryWrapper: LeadRepositoryWrapper
) : LeadDocumentsService {

    @Transactional
    override fun createDocumentForLead(
        leadId: UUID,
        file: MultipartFile,
        createRequest: LeadDocumentCreateRequest
    ): LeadDocumentCreateResponse {
        val lead = leadRepositoryWrapper.findByIdWithException(leadId)

        createRequest.taskId?.let { validateTaskBelongsToLead(it, lead) }

        val resolvedFileName = createRequest.fileName

        val documentResponse = documentWriteService.createDocument(
            DocumentCreateRequest(
                name = resolvedFileName,
                file = file,
                tags = createRequest.tags,
                customPath = resolveLeadDocumentPath(leadId, createRequest.taskId, resolvedFileName)
            )
        )

        val newLeadDocumentData = LeadDocumentData(
            documentId = documentResponse.id,
            taskId = createRequest.taskId,
            createdDate = documentResponse.createdAt
        )

        val updatedDocuments = lead.documentIds?.toMutableList() ?: mutableListOf()
        updatedDocuments.add(newLeadDocumentData)
        lead.documentIds = updatedDocuments

        leadRepositoryWrapper.saveWithException(lead)

        return LeadDocumentCreateResponse(documentId = documentResponse.id)
    }

    override fun getDocumentsForLead(
        leadId: UUID,
        paginationRequest: PaginationRequest
    ): PaginatedResponse<LeadDocumentResponse> {
        val lead = leadRepositoryWrapper.findByIdWithException(leadId)

        return lead.documentIds
            .orEmpty()
            .sortedByDescending { it.createdDate }
            .map { it.toLeadDocumentResponse(documentReadService) }
            .toBasicPaginatedResponse()
    }

    override fun getDocumentsForLeadTasks(
        leadId: UUID,
        taskId: UUID,
        paginationRequest: PaginationRequest
    ): PaginatedResponse<LeadDocumentResponse> {
        val lead = leadRepositoryWrapper.findByIdWithException(leadId)

        return lead.documentIds
            .orEmpty()
            .filter { it.taskId == taskId }
            .sortedByDescending { it.createdDate }
            .map { it.toLeadDocumentResponse(documentReadService) }
            .toBasicPaginatedResponse()
    }

    @Transactional
    override fun deleteDocumentFromLead(leadId: UUID, documentId: UUID) {
        val lead = leadRepositoryWrapper.findByIdWithException(leadId)
        val documentExists = lead.documentIds?.any { it.documentId == documentId } ?: false
        if (!documentExists) {
            throw IllegalArgumentException("Document with id $documentId does not belong to lead with id ${lead.id}")
        }
        documentWriteService.deleteDocumentById(documentId)
        lead.documentIds = lead.documentIds?.filter { it.documentId != documentId }
        leadRepositoryWrapper.saveWithException(lead)
    }

    private fun validateTaskBelongsToLead(taskId: UUID, lead: Lead) {
        val taskExists = lead.taskData?.any { it.taskId == taskId } ?: false
        if (!taskExists) {
            throw IllegalArgumentException("Task with id $taskId does not belong to lead with id ${lead.id}")
        }
    }


    private fun buildPaginatedResponse(content: List<LeadDocumentResponse>): PaginatedResponse<LeadDocumentResponse> {
        val totalElements = content.size.toLong()
        val hasContent = totalElements > 0

        val paginationInfo = PaginationInfo(
            offset = 0,
            limit = content.size,
            totalElements = totalElements,
            totalPages = if (hasContent) 1 else 0,
            currentPage = if (hasContent) 1 else 0,
            hasNext = false,
            hasPrevious = false
        )

        return PaginatedResponse(
            content = content,
            pagination = paginationInfo
        )
    }
}
