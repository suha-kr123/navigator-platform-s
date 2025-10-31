package com.nivasafinance.features.document.dto

import java.time.LocalDateTime
import java.util.UUID

data class DocumentCreateResponse(
    val id: Long,
    val createdAt: LocalDateTime
)
