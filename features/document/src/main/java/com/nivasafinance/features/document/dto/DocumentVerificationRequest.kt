package com.nivasafinance.features.document.dto

data class DocumentVerificationRequest(
    val isVerified: Boolean,
    val verificationNotes: String? = null
)
