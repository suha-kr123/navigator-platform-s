package com.nivasafinance.features.lead.dto

import com.nivasafinance.features.identifiers.dto.IdentifierRequest

data class LeadIdentifierRequest(
    val identifier: String,
    val type: String,
    val extData: Map<String, Any>? = null
) {
    fun toIdentifierRequest(): IdentifierRequest {
        return IdentifierRequest(
            identifier = identifier,
            type = type,
            extData = extData
        )
    }
}
