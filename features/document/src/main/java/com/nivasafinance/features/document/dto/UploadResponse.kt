package com.nivasafinance.features.document.dto

import annotations.NoArg
import java.util.UUID

@NoArg
data class UploadResponse(
    val documentId: UUID,
    val uploadUrl: String? // client uses this to PUT file directly
)
