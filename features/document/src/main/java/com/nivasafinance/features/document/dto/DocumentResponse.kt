package com.nivasafinance.features.document.dto

import com.nivasafinance.features.document.entity.Document
import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse
import com.nivasafinance.features.master.codemaster.dto.MasterCodeWithValuesResponse
import java.time.LocalDateTime
import java.util.UUID

data class DocumentResponse(
    val id: Long,
    val name: String,
    val type: String?,
    val size: Long?,
    val createdAt: LocalDateTime,
    val createdBy: String?,
    val updatedAt: LocalDateTime,
    val updatedBy: String?
)

fun Document.toDocumentResponse(codeValues: List<MasterCodeWithValuesResponse>): DocumentResponse {
    val documentId = id ?: error("Document ID cannot be null")
    val createdAt = createdAt ?: error("Document createdAt cannot be null")
    val updatedAt = updatedAt ?: error("Document updatedAt cannot be null")
    return DocumentResponse(
        id = documentId,
        name = name,
        type = type,
        size = size,
        createdAt = createdAt,
        createdBy = createdBy,
        updatedAt = updatedAt,
        updatedBy = updatedBy
    )
}
