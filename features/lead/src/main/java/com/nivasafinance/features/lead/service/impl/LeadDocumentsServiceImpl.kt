package com.nivasafinance.features.lead.service.impl

import base.model.PaginatedResponse
import base.model.PaginationInfo
import base.model.PaginationRequest
import com.nivasafinance.features.document.dto.DocumentRequest
import com.nivasafinance.features.document.dto.DocumentResponse
import com.nivasafinance.features.document.service.DocumentService
import com.nivasafinance.features.lead.dto.LeadDocumentsResponse
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
    private fun DocumentResponse.toLeadDocumentsResponse(leadId: UUID, taskId: UUID?): LeadDocumentsResponse {
        return LeadDocumentsResponse(
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

    override fun addDocumentsToLead(leadId: UUID, taskId: UUID?, addDocumentsToLeadRequest: DocumentRequest): LeadDocumentsResponse {
        // Verify lead exists
        val lead = leadRepositoryWrapper.findByIdWithException(leadId)

        // If taskId is provided, verify that it belongs to this lead
        if (taskId != null) {
            val taskExists = lead.taskData?.any { it.taskId == taskId } ?: false
            if (!taskExists) {
                throw IllegalArgumentException("Task with ID $taskId does not belong to lead $leadId")
            }
        }

        // Create document using DocumentService
        // For now, create an empty input stream since we're not handling file uploads in this method
        // In a real implementation, you might want to handle file uploads differently
        val emptyInputStream = ByteArrayInputStream(ByteArray(0))
        val documentResponse = documentService.createDocument(addDocumentsToLeadRequest, emptyInputStream)

        // Update the lead's task data to include the document ID (only if taskId is provided)
        if (taskId != null) {
            val currentTaskData = lead.taskData ?: emptyList()
            val updatedTaskData = currentTaskData.map { taskData ->
                if (taskData.taskId == taskId) {
                    val currentDocumentIds = taskData.documentIds
                    taskData.copy(documentIds = currentDocumentIds + documentResponse.documentId)
                } else {
                    taskData
                }
            }
            lead.taskData = updatedTaskData
        }

        // Also update the lead's documentIds list
        val currentDocumentIds = lead.documentIds ?: emptyList()
        lead.documentIds = (currentDocumentIds + documentResponse.documentId).distinct()

        leadRepositoryWrapper.saveWithException(lead)

        return documentResponse.toLeadDocumentsResponse(leadId, taskId)
    }

    override fun getTaskDocuments(leadId: UUID, taskId: UUID?, paginationRequest: PaginationRequest): PaginatedResponse<List<LeadDocumentsResponse>> {
        // Verify lead exists
        val lead = leadRepositoryWrapper.findByIdWithException(leadId)

        // If taskId is provided, verify that it belongs to this lead
        if (taskId != null) {
            val taskExists = lead.taskData?.any { it.taskId == taskId } ?: false
            if (!taskExists) {
                throw IllegalArgumentException("Task with ID $taskId does not belong to lead $leadId")
            }
        }

        // Get documents for the specific task or all lead documents
        val documentIds = if (taskId != null) {
            val taskData = lead.taskData?.find { it.taskId == taskId }
            taskData?.documentIds ?: emptyList()
        } else {
            lead.documentIds ?: emptyList()
        }

        // Convert to LeadTaskDocumentsResponse
        val leadDocumentsResponses = documentIds.map { documentId ->
            val document = documentService.getDocumentById(documentId)
            document.toLeadDocumentsResponse(leadId, taskId)
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

    override fun deleteDocumentById(leadId: UUID, taskId: UUID?, documentId: UUID) {
        // Verify lead exists and contains the document
        val lead = leadRepositoryWrapper.findByIdWithException(leadId)

        // If taskId is provided, verify that it belongs to this lead
        if (taskId != null) {
            val taskExists = lead.taskData?.any { it.taskId == taskId } ?: false
            if (!taskExists) {
                throw IllegalArgumentException("Task with ID $taskId does not belong to lead $leadId")
            }
        }

        // Check if document exists in the lead's task data or general document list
        val documentExists = if (taskId != null) {
            // Check specific task
            lead.taskData?.any { taskData ->
                taskData.taskId == taskId && taskData.documentIds.contains(documentId)
            } ?: false
        } else {
            // Check all tasks or lead's general document list
            val existsInTasks = lead.taskData?.any { taskData ->
                taskData.documentIds.contains(documentId)
            } ?: false
            val existsInLeadDocs = lead.documentIds?.contains(documentId) ?: false
            existsInTasks || existsInLeadDocs
        }

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

        // Also remove from lead's documentIds list
        val currentDocumentIds = lead.documentIds ?: emptyList()
        lead.documentIds = currentDocumentIds.filter { it != documentId }

        leadRepositoryWrapper.saveWithException(lead)

        // Delete the document
        documentService.deleteDocumentById(documentId)
    }

    override fun getAllDocuments(paginationRequest: PaginationRequest): PaginatedResponse<List<LeadDocumentsResponse>> {
        // This would typically query all documents across all leads
        // For now, return empty list - implement based on your requirements
        return PaginatedResponse(
            content = emptyList(),
            pagination = PaginationInfo(
                offset = paginationRequest.offset,
                limit = paginationRequest.limit,
                totalElements = 0,
                totalPages = 0,
                currentPage = 1,
                hasNext = false,
                hasPrevious = false
            )
        )
    }
}
