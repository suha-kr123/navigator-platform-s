package com.nivasafinance.features.document.dto

import annotations.NoArg

@NoArg
data class UploadRequest(
    val fileName: String,
    val fileType: String?,
    val fileSize: Long?,
    val category: String?,
    val docType: String?,
    val tags: List<String>
)
