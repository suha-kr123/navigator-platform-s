package com.nivasafinance.features.lead.service.impl

import base.model.PaginatedResponse
import base.model.PaginationInfo
import base.model.PaginationRequest
import com.nivasafinance.features.document.dto.DocumentRequest
import com.nivasafinance.features.document.dto.DocumentResponse
import com.nivasafinance.features.document.service.DocumentService
import com.nivasafinance.features.lead.dto.LeadTaskDocumentsResponse
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper
import com.nivasafinance.features.lead.service.LeadDocumentsService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.ByteArrayInputStream
import java.util.*

@Service
@Transactional
class LeadDocumentsServiceImpl(
    private val documentService: DocumentService,
    private val leadRepositoryWrapper: LeadRepositoryWrapper
) : LeadDocumentsService {

    /**
     * Converts DocumentResponse to LeadTaskDocumentsResponse
     */
    private fun DocumentResponse.toLeadTaskDocumentsResponse(leadId: UUID, taskId: UUID): LeadTaskDocumentsResponse {
        return LeadTaskDocumentsResponse(
            leadId = leadId,
            taskId = taskId,
            documentId = this.documentId,
            documentName = this.fileName,
            documentUrl = this.fileUrl ?: "",
            documentType = this.documentType,
            documentVerificationStatus = this.verificationStatus,
            documentVerificationNotes = this.verificationNotes,
            documentFileSize = this.fileSize,
            documentStorageKey = this.storageKey,
            documentFileUrl = this.fileUrl,
            documentCategory = this.category,
            documentDocType = this.docType,
            documentTags = this.tags,
            documentExtData = this.extData,
            documentCreatedAt = this.createdAt,
            documentCreatedBy = this.createdBy ?: "system",
            documentUpdatedAt = this.updatedAt,
            documentUpdatedBy = this.updatedBy ?: "system"
        )
    }

    override fun addDocumentsToLead(leadId: UUID, taskId: UUID, addDocumentsToLeadRequest: DocumentRequest): LeadTaskDocumentsResponse {
        // Verify lead exists
        val lead = leadRepositoryWrapper.findByIdWithException(leadId)

        // Verify that the taskId belongs to this lead
        val taskExists = lead.taskData?.any { it.taskId == taskId } ?: false
        if (!taskExists) {
            throw IllegalArgumentException("Task with ID $taskId does not belong to lead $leadId")
        }

        // Create document using DocumentService
        // For now, create an empty input stream since we're not handling file uploads in this method
        // In a real implementation, you might want to handle file uploads differently
        val emptyInputStream = ByteArrayInputStream(ByteArray(0))
        val documentResponse = documentService.createDocument(addDocumentsToLeadRequest, emptyInputStream)

        // Update the lead's task data to include the document ID
        val currentTaskData = lead.taskData ?: emptyList()
        val updatedTaskData = currentTaskData.map { taskData ->
            if (taskData.taskId == taskId) {
                val currentDocumentIds = taskData.documentIds
                taskData.copy(documentIds = currentDocumentIds + documentResponse.documentId)
            } else {
                taskData
            }
        }

        // Update the lead entity directly instead of using copy() to preserve version
        lead.taskData = updatedTaskData
        leadRepositoryWrapper.saveWithException(lead)

        return documentResponse.toLeadTaskDocumentsResponse(leadId, taskId)
    }

    override fun getDocumentById(leadId: UUID, taskId: UUID, documentId: UUID): LeadTaskDocumentsResponse {
        // Verify lead exists and contains the document
        val lead = leadRepositoryWrapper.findByIdWithException(leadId)

        // Verify that the taskId belongs to this lead
        val taskExists = lead.taskData?.any { it.taskId == taskId } ?: false
        if (!taskExists) {
            throw IllegalArgumentException("Task with ID $taskId does not belong to lead $leadId")
        }

        val documentExists = lead.taskData?.any { taskData ->
            taskData.taskId == taskId && taskData.documentIds.contains(documentId)
        } ?: false

        if (!documentExists) {
            throw IllegalArgumentException(
                "Document with ID $documentId does not belong to task $taskId in lead $leadId"
            )
        }

        val document = documentService.getDocumentById(documentId)
        return document.toLeadTaskDocumentsResponse(leadId, taskId)
    }

    override fun getLeadDocuments(leadId: UUID, paginationRequest: PaginationRequest): PaginatedResponse<List<LeadTaskDocumentsResponse>> {
        // Verify lead exists
        val lead = leadRepositoryWrapper.findByIdWithException(leadId)

        // Get all document IDs from the lead's task data
        val allDocumentIds = lead.taskData?.flatMap { taskData ->
            taskData.documentIds
        } ?: emptyList()

        if (allDocumentIds.isEmpty()) {
            return PaginatedResponse(
                content = listOf(emptyList()),
                pagination = PaginationInfo(
                    offset = paginationRequest.offset,
                    limit = paginationRequest.limit,
                    totalElements = 0,
                    totalPages = 0,
                    currentPage = 0,
                    hasNext = false,
                    hasPrevious = false
                )
            )
        }

        // Get all documents and filter by the documentIds from the lead
        val allDocuments = documentService.getAllDocuments()
        val filteredDocuments = allDocuments.filter { document ->
            allDocumentIds.contains(document.documentId)
        }

        // Convert to LeadTaskDocumentsResponse with lead ID and task ID
        val leadDocumentsResponses = filteredDocuments.map { document ->
            // We need to find which task this document belongs to
            val taskData = lead.taskData?.find { it.documentIds.contains(document.documentId) }
            val taskId = taskData?.taskId ?: UUID.randomUUID() // fallback if not found
            document.toLeadTaskDocumentsResponse(leadId, taskId)
        }

        // Create a new paginated response with filtered documents
        return PaginatedResponse(
            content = listOf(leadDocumentsResponses),
            pagination = PaginationInfo(
                offset = paginationRequest.offset,
                limit = paginationRequest.limit,
                totalElements = leadDocumentsResponses.size.toLong(),
                totalPages = if (leadDocumentsResponses.isEmpty()) 0 else 1,
                currentPage = 0,
                hasNext = false,
                hasPrevious = false
            )
        )
    }

    override fun getTaskDocuments(leadId: UUID, taskId: UUID, paginationRequest: PaginationRequest): PaginatedResponse<List<LeadTaskDocumentsResponse>> {
        // Verify lead exists
        val lead = leadRepositoryWrapper.findByIdWithException(leadId)

        // Verify that the taskId belongs to this lead
        val taskExists = lead.taskData?.any { it.taskId == taskId } ?: false
        if (!taskExists) {
            throw IllegalArgumentException("Task with ID $taskId does not belong to lead $leadId")
        }

        // Get documents for the specific task
        val taskData = lead.taskData?.find { it.taskId == taskId }
        val documentIds = taskData?.documentIds ?: emptyList()

        // Convert to LeadTaskDocumentsResponse
        val leadDocumentsResponses = documentIds.map { documentId ->
            val document = documentService.getDocumentById(documentId)
            document.toLeadTaskDocumentsResponse(leadId, taskId)
        }

        // For now, return all documents without pagination
        // In a real implementation, you might want to implement proper pagination
        return PaginatedResponse(
            content = listOf(leadDocumentsResponses),
            pagination = PaginationInfo(
                offset = paginationRequest.offset,
                limit = paginationRequest.limit,
                totalElements = leadDocumentsResponses.size.toLong(),
                totalPages = 1,
                currentPage = 1,
                hasNext = false,
                hasPrevious = false
            )
        )
    }

    override fun getAllLeadsDocuments(paginationRequest: PaginationRequest): PaginatedResponse<List<LeadTaskDocumentsResponse>> {
        // Get all leads that have documents
        val pageable = org.springframework.data.domain.PageRequest.of(
            paginationRequest.offset / paginationRequest.limit,
            paginationRequest.limit
        )
        val leadsPage = leadRepositoryWrapper.findAllWithException(pageable)

        val allLeadTaskDocumentsResponses = mutableListOf<LeadTaskDocumentsResponse>()

        leadsPage.content.forEach { lead ->
            lead.taskData?.forEach { taskData ->
                val documentIds = taskData.documentIds
                documentIds.forEach { documentId ->
                    try {
                        val document = documentService.getDocumentById(documentId)
                        allLeadTaskDocumentsResponses.add(
                            document.toLeadTaskDocumentsResponse(lead.id!!, taskData.taskId)
                        )
                    } catch (e: Exception) {
                        // Skip documents that no longer exist
                    }
                }
            }
        }

        val totalPages = if (allLeadTaskDocumentsResponses.isEmpty()) 0 else ((allLeadTaskDocumentsResponses.size - 1) / paginationRequest.limit + 1)
        val currentPage = paginationRequest.offset / paginationRequest.limit

        return PaginatedResponse(
            content = listOf(allLeadTaskDocumentsResponses),
            pagination = PaginationInfo(
                offset = paginationRequest.offset,
                limit = paginationRequest.limit,
                totalElements = allLeadTaskDocumentsResponses.size.toLong(),
                totalPages = totalPages,
                currentPage = currentPage,
                hasNext = currentPage < totalPages - 1,
                hasPrevious = currentPage > 0
            )
        )
    }

    override fun deleteDocumentById(leadId: UUID, taskId: UUID, documentId: UUID) {
        // Verify lead exists and contains the document
        val lead = leadRepositoryWrapper.findByIdWithException(leadId)

        // Verify that the taskId belongs to this lead
        val taskExists = lead.taskData?.any { it.taskId == taskId } ?: false
        if (!taskExists) {
            throw IllegalArgumentException("Task with ID $taskId does not belong to lead $leadId")
        }

        val documentExists = lead.taskData?.any { taskData ->
            taskData.taskId == taskId && taskData.documentIds.contains(documentId)
        } ?: false

        if (!documentExists) {
            throw IllegalArgumentException(
                "Document with ID $documentId does not belong to task $taskId in lead $leadId"
            )
        }

        // Remove document from lead's task data
        val updatedTaskData = lead.taskData?.map { taskData ->
            val updatedDocumentIds = taskData.documentIds.filter { it != documentId }
            taskData.copy(documentIds = updatedDocumentIds)
        } ?: emptyList()

        // Update the lead entity directly instead of using copy() to preserve version
        lead.taskData = updatedTaskData
        leadRepositoryWrapper.saveWithException(lead)

        // Delete the document
        documentService.deleteDocumentById(documentId)
    }
}