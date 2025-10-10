package com.nivasafinance.features.lead.dto

import com.nivasafinance.features.lead.entity.RequestedAmountRange
import com.nivasafinance.features.lead.enum.LeadPersonType
import com.nivasafinance.features.person.entity.MobileNumberDetails
import com.nivasafinance.features.person.enum.Gender
import java.time.LocalDate

data class LeadCreateRequest(

    val requestedAmountRange: RequestedAmountRange?,
    val purpose: String?,
    val productCode: String?,
    val sourcingChannel: String?,
    val preliminaryInformation: LeadPreliminaryInformation?,
    val extData: Map<String, Any>?,
    val leadPersons: List<LeadPersonRequest>?
)

data class LeadPersonRequest(
    val firstName: String?,
    val middleName: String?,
    val lastName: String?,
    val mobileNumbers: List<MobileNumberDetails>?,
    val dateOfBirth: LocalDate?,
    val gender: Gender?,
    val extData: Map<String, Any>?,
    val leadPersonType: LeadPersonType?,
    val relationshipToPrimary: String?
)
