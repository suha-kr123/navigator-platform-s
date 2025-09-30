package com.nivasafinance.features.master.codemaster.dto

import java.util.UUID

data class CodeValueResponse(
    val id: UUID?,
    val key: String,
    val value: String,
    val description: String?,
    val isActive: Boolean
)
