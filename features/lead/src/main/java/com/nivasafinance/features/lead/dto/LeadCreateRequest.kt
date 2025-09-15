package com.nivasafinance.features.lead.dto

import com.nivasafinance.features.lead.dto.LeadPreliminaryInformation
import com.nivasafinance.features.lead.dto.LeadContacts
import com.nivasafinance.features.lead.enum.SourcingChannel
import java.math.BigDecimal

data class LeadCreateRequest(

    val requestedAmount: BigDecimal?,
    val purpose: String?,
    val productCode: String?,
    val pipelineKey: String?,
    val sourcingChannel: SourcingChannel?,
    val preliminaryInformation: LeadPreliminaryInformation?,
    val leadContacts: LeadContacts?,
    val extData: Map<String, Any>?
)