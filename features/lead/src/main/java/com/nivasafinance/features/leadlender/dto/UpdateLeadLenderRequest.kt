package com.nivasafinance.features.leadlender.dto

data class UpdateLeadLenderRequest(
    val lenderOfficeKey: String? = null,
    val loginId: String? = null,
    val rmDetails: RmDetails? = null
)
