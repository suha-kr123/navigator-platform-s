package com.nivasafinance.features.document.dto

import com.nivasafinance.features.document.enum.ProviderType
import com.nivasafinance.features.document.enum.VerificationStatus
import java.util.*

data class DocumentRequest(
    val entityId: UUID,
    val entityType: String,
    val documentType: String,
    val verificationStatus: VerificationStatus = VerificationStatus.PENDING_TO_BE_VERIFIED,
    val verificationNotes: String? = null,
    val fileName: String,
    val fileType: String? = null,
    val fileSize: Long? = null,
    val provider: ProviderType,
    val storageKey: String,
    val fileUrl: String? = null,
    val tags: List<String>? = null
)
