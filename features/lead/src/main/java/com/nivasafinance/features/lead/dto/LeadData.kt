package com.nivasafinance.features.lead.dto

import com.nivasafinance.features.lead.entity.Lead
import com.nivasafinance.features.lead.enum.LeadStage
import com.nivasafinance.features.lead.enum.LeadStatus
import com.nivasafinance.features.lead.enum.SourcingChannel
import java.math.BigDecimal
import java.util.UUID

data class LeadData(
    val id: UUID?,
    val requestedAmount: BigDecimal?,
    val purpose: String?,
    val productCode: String?,
    val status: LeadStatus?,
    val stage: LeadStage?,
    val preliminaryInformation: LeadPreliminaryInformation?,
    val leadContacts: LeadContacts?,
    val sourcingChannel: SourcingChannel?,
    val extData: Map<String, Any>?
) {
    companion object {
        fun fromEntity(lead: Lead): LeadData {
            return LeadData(
                id = lead.id,
                requestedAmount = lead.requestedAmount,
                purpose = lead.purpose,
                productCode = lead.productCode,
                status = lead.status,
                stage = lead.stage,
                preliminaryInformation = lead.preliminaryInformation,
                leadContacts = lead.leadContacts,
                sourcingChannel = lead.sourcingChannel,
                extData = lead.extData
            )
        }
    }
}
