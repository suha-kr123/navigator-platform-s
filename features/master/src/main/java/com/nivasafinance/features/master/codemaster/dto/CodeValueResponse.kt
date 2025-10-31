package com.nivasafinance.features.master.codemaster.dto

import com.nivasafinance.features.master.codemaster.entity.MasterCodeValue
import java.util.UUID

data class CodeValueResponse(
    val id: UUID?,
    val key: String,
    val value: String,
    val description: String?,
    val isActive: Boolean
)

fun MasterCodeValue.mapToCodeValueResponse(): CodeValueResponse {
    return CodeValueResponse(
        id = id,
        key = key,
        value = value?.defaultValue.orEmpty(),
        description = description?.defaultValue.orEmpty(),
        isActive = isActive
    )
}