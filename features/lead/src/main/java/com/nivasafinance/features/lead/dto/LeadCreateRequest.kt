package com.nivasafinance.features.lead.dto

import com.nivasafinance.features.person.entity.MobileNumberDetails
import com.nivasafinance.features.person.enum.Gender
import java.math.BigDecimal

data class LeadCreateRequest(

    val requestedAmountRange: Map<String, BigDecimal>?,
    val purpose: String?,
    val productCode: String?,
    val sourcingChannel: String?,
    val preliminaryInformation: LeadPreliminaryInformation?,
    val extData: Map<String, Any>?,
    val leadPersons: List<PersonRequest>?
)

data class PersonRequest(
    val firstName: String?,
    val middleName: String?,
    val lastName: String?,
    val mobileNumbers: List<MobileNumberDetails>?,
    val dateOfBirth: String?,
    val gender: Gender?,
    val extData: Map<String, Any>?,
    val applicantType: String?,
    val relationshipToPrimary: String?
)
