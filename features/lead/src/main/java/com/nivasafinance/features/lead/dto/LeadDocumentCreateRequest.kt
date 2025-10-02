package com.nivasafinance.features.lead.dto

import jakarta.validation.constraints.NotBlank
import java.util.UUID

data class LeadDocumentCreateRequest(
    val taskId: UUID? = null,
    @field:NotBlank(message = "File name is required")
    val fileName: String,
    val tags: List<String>? = null
)
