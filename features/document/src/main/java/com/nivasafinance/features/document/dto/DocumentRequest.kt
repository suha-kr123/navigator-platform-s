package com.nivasafinance.features.document.dto

import com.nivasafinance.features.document.enum.ProviderType
import java.util.*

data class DocumentRequest(
    val entityId: UUID,
    val entityType: String,
    val documentType: String,
    val fileName: String,
    val fileType: String? = null,
    val fileSize: Long? = null,
    val fileUrl: String? = null,
    val category: String? = null,
    val docType: String? = null,
    val tags: List<String>? = null,
    val extData: Map<String, Any>? = null
)
