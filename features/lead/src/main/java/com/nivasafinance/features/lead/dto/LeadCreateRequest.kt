package com.nivasafinance.features.lead.dto

import com.nivasafinance.features.lead.enum.LeadStage
import com.nivasafinance.features.lead.enum.LeadStatus
import com.nivasafinance.features.lead.enum.SourcingChannel
import java.math.BigDecimal

data class LeadCreateRequest(
    val requestedAmount: BigDecimal?,
    val purpose: String? = null,
    val productCode: String? = null,
    val sourcingChannel: SourcingChannel? = null,
    val preliminaryInformation: LeadPreliminaryInformation? = null,
    val leadContacts: LeadContacts? = null,
    val stage: LeadStage? = null,
    val status: LeadStatus? = null,
    val extData: Map<String, Any>? = null
)

data class LeadPreliminaryInformation(
    val whenYouWantLoan: String,
    val isHouseConstructionStarted: Boolean,
    val isEKhathaAvailable: Boolean,
    val selfDeclaredAnnualFamilyIncome: Int,
    val preferredCallTime: String? = null,
    val monthlyIncome: BigDecimal? = null
)

data class LeadContacts(
    val name: String,
    val number: String
)
