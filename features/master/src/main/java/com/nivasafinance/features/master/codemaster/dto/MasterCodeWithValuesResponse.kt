package com.nivasafinance.features.master.codemaster.dto

import java.util.UUID

data class MasterCodeWithValuesResponse(
    val id: UUID?,
    val key: String,
    val name: String,
    val description: String?,
    val isSystemDefined: Boolean,
    val parentId: UUID?,
    val values: List<CodeValueResponse>
)
