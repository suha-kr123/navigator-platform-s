package com.nivasafinance.features.lead.lead.dto

import com.nivasafinance.features.lead.applicant.dto.ApplicantCreateRequest
import com.nivasafinance.features.lead.applicant.enum.ApplicantType
import com.nivasafinance.features.lead.applicant.enum.RelationshipToPrimary
import com.nivasafinance.features.lead.lead.enum.LeadStage
import com.nivasafinance.features.lead.lead.enum.LeadStatus
import com.nivasafinance.features.person.dto.PersonDto
import java.math.BigDecimal
import java.util.UUID

data class LeadCreateRequest(
    val id: UUID = UUID.randomUUID(),
    val requestedAmount: BigDecimal?,
    val purpose: String? = null,
    val productCode: String? = null,
    val sourcingChannel: String? = null,
    val preimerlyInformation: LeadPreimerlyInformation? = null,
    val leadContacts: LeadContacts? = null,
    val applicantDetails: ApplicantCreateRequest = ApplicantCreateRequest(
        personalDetails = PersonDto(),
        applicantType = ApplicantType.PRIMARY,
        relationshipToPrimary = RelationshipToPrimary.SELF,
        description = null
    ),
    val stage: LeadStage? = null,
    val status: LeadStatus? = null
)

data class LeadPreimerlyInformation(
    val whenYouWantLoan: String,
    val isHouseConstructionStarted: Boolean,
    val isEKhathaAvailable: Boolean,
    val selfDeclaredAnnualFamilyIncome: Int
)

data class LeadContacts(
    val name: String,
    val number: String
)
