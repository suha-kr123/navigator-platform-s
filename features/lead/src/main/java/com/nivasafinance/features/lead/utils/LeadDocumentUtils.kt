package com.nivasafinance.features.lead.utils

import com.nivasafinance.features.document.service.DocumentReadService
import com.nivasafinance.features.lead.dto.LeadDocumentResponse
import com.nivasafinance.features.lead.entity.LeadDocumentData
import java.util.UUID

private val nonCompliantChars = Regex("[^A-Za-z0-9._-]")

private fun sanitizeFileName(fileName: String): String {
    val trimmed = fileName.trim().ifEmpty { "document" }
    val normalisedWhitespace = trimmed.replace("\\s+".toRegex(), "_")
    return normalisedWhitespace.replace(nonCompliantChars, "_")
}

fun generateDocumentPathForLead(
    leadId: UUID,
    fileName: String
): String {
    val sanitisedFileName = sanitizeFileName(fileName)
    return "leads/${leadId}/${sanitisedFileName}_${System.currentTimeMillis()}"
}

fun generateDocumentPathForLeadTask(
    leadId: UUID,
    taskId: UUID,
    fileName: String
): String {
    val sanitisedFileName = sanitizeFileName(fileName)
    return "leads/${leadId}/tasks/${taskId}/${sanitisedFileName}_${System.currentTimeMillis()}"
}

fun resolveLeadDocumentPath(
    leadId: UUID,
    taskId: UUID?,
    fileName: String
): String = if (taskId == null) {
    generateDocumentPathForLead(leadId, fileName)
} else {
    generateDocumentPathForLeadTask(leadId, taskId, fileName)
}

fun LeadDocumentData.toLeadDocumentResponse(documentReadService: DocumentReadService): LeadDocumentResponse {
    val document = documentReadService.getDocumentById(documentId)
    return LeadDocumentResponse(
        taskId = taskId,
        document = document
    )
}
