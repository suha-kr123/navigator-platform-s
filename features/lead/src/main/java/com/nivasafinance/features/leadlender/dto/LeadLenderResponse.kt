package com.nivasafinance.features.leadlender.dto

import com.nivasafinance.features.leadlender.enum.LeadLenderStatus
import com.nivasafinance.features.lender.lender.dto.LenderResponseData
import com.nivasafinance.features.lender.lenderoffice.dto.LenderOfficeReponseData
import java.util.UUID

data class LeadLenderResponse(
    val id: UUID,
    val leadId: UUID,
    val status: LeadLenderStatus,
    val loginId: String? = null,
    val lender: LenderResponseData,
    val lenderOffice: LenderOfficeReponseData? = null,
    val relationshipManager: RmDetails? = null
)
