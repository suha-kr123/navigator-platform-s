package com.nivasafinance.features.document.dto

import java.util.UUID

data class DocumentCreateResponse(
    val id: Long,
    val identifier: UUID,
)
