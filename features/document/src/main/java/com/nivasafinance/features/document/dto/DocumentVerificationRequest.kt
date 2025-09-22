package com.nivasafinance.features.document.dto

import com.nivasafinance.features.document.enum.VerificationStatus

data class DocumentVerificationRequest(
    val verificationStatus: VerificationStatus,
    val verificationNotes: String? = null
)
