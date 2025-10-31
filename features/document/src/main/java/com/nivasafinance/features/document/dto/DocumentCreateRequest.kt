package com.nivasafinance.features.document.dto

import org.springframework.web.multipart.MultipartFile

data class DocumentCreateRequest(
    val name: String,
    val file: MultipartFile,
    val customPath: String? = null // Allow custom folder structure
)
