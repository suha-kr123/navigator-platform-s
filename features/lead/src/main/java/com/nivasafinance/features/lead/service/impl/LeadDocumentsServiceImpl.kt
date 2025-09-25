package com.nivasafinance.features.lead.service.impl

import base.model.PaginatedResponse
import base.model.PaginationInfo
import base.model.PaginationRequest
import com.nivasafinance.features.lead.dto.LeadTaskDocumentsResponse
import com.nivasafinance.features.document.dto.DocumentVerificationRequest
import com.nivasafinance.features.lead.entity.TaskData
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper
import com.nivasafinance.features.lead.service.LeadDocumentsService
import com.nivasafinance.features.document.dto.DocumentRequest
import com.nivasafinance.features.document.dto.DocumentResponse
import com.nivasafinance.features.document.service.DocumentService
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
        
        return LeadTaskDocumentsResponse(
            leadId = leadId,
            taskId = taskId,
            documents = listOf(documentResponse)
        )
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
            throw IllegalArgumentException("Document with ID $documentId does not belong to task $taskId in lead $leadId")
        }

        val document = documentService.getDocumentById(documentId)
        return LeadTaskDocumentsResponse(
            leadId = leadId,
            taskId = taskId,
            documents = listOf(document)
        )
    }

    override fun getLeadDocuments(leadId: UUID, paginationRequest: PaginationRequest): PaginatedResponse<LeadTaskDocumentsResponse> {
        // Verify lead exists
        val lead = leadRepositoryWrapper.findByIdWithException(leadId)
        
        // Get all document IDs from the lead's task data
        val allDocumentIds = lead.taskData?.flatMap { taskData ->
            taskData.documentIds
        } ?: emptyList()
        
        if (allDocumentIds.isEmpty()) {
            return PaginatedResponse(
                content = emptyList(),
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
            LeadTaskDocumentsResponse(
                leadId = leadId,
                taskId = taskData?.taskId ?: UUID.randomUUID(), // fallback if not found
                documents = listOf(document)
            )
        }

        // Create a new paginated response with filtered documents
        return PaginatedResponse(
            content = leadDocumentsResponses,
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

    override fun getTaskDocuments(leadId: UUID, taskId: UUID, paginationRequest: PaginationRequest): PaginatedResponse<LeadTaskDocumentsResponse> {
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
            LeadTaskDocumentsResponse(
                leadId = leadId,
                taskId = taskId,
                documents = listOf(document)
            )
        }
        
        // For now, return all documents without pagination
        // In a real implementation, you might want to implement proper pagination
        return PaginatedResponse(
            content = leadDocumentsResponses,
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

    override fun getAllLeadsDocuments(paginationRequest: PaginationRequest): PaginatedResponse<LeadTaskDocumentsResponse> {
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
                            LeadTaskDocumentsResponse(
                                leadId = lead.id!!,
                                taskId = taskData.taskId,
                                documents = listOf(document)
                            )
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
            content = allLeadTaskDocumentsResponses,
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


    override fun verifyDocument(leadId: UUID, taskId: UUID, documentId: UUID, verifyDocumentRequest: DocumentVerificationRequest): LeadTaskDocumentsResponse {
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
            throw IllegalArgumentException("Document with ID $documentId does not belong to task $taskId in lead $leadId")
        }
        
        // For now, we'll just return the existing document since DocumentService doesn't have verification methods
        // In a real implementation, you would need to add verification methods to DocumentService
        val document = documentService.getDocumentById(documentId)
        return LeadTaskDocumentsResponse(
            leadId = leadId,
            taskId = taskId,
            documents = listOf(document)
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
            throw IllegalArgumentException("Document with ID $documentId does not belong to task $taskId in lead $leadId")
        }
        
        // Remove document from lead's task data
        val updatedTaskData = lead.taskData?.map { taskData ->
            if (taskData.taskId == taskId) {
                val updatedDocumentIds = taskData.documentIds.filter { it != documentId }
                taskData.copy(documentIds = updatedDocumentIds)
            } else {
                taskData
            }
        } ?: emptyList()
        
        // Update the lead entity directly
        lead.taskData = updatedTaskData
        leadRepositoryWrapper.saveWithException(lead)
        
        // Delete the document
        documentService.deleteDocumentById(documentId)
    }
}
