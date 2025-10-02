package com.nivasafinance.features.document.dto

import com.nivasafinance.features.document.entity.Document
import com.nivasafinance.features.master.codemaster.dto.CodeMasterListResponse
import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse
import java.time.LocalDateTime
import java.util.UUID

data class DocumentResponse(
    val id: UUID,
    val name: String,
    val type: String?,
    val size: Long?,
    val tags: List<CodeValueResponse>?,
    val createdAt: LocalDateTime,
    val createdBy: String?,
    val updatedAt: LocalDateTime,
    val updatedBy: String?
)

fun Document.toDocumentResponse(codeValues: List<CodeMasterListResponse>): DocumentResponse {
    val documentId = id ?: error("Document ID cannot be null")
    val createdAt = createdAt ?: error("Document createdAt cannot be null")
    val updatedAt = updatedAt ?: error("Document updatedAt cannot be null")
    val tags: List<CodeValueResponse>? =
        codeValues.map { it.values }.flatten().filter { tags?.contains(it.key) == true }
    return DocumentResponse(
        id = documentId,
        name = name,
        type = type,
        size = size,
        tags = tags,
        createdAt = createdAt,
        createdBy = createdBy,
        updatedAt = updatedAt,
        updatedBy = updatedBy
    )
}