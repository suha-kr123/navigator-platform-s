package com.nivasafinance.features.document.dto

import java.io.InputStream

data class DocumentFileResponse(
    val file: InputStream,
    val data: DocumentResponse
)