package com.nivasafinance.features.lead.dto

import com.nivasafinance.features.lead.entity.Lead
import com.nivasafinance.features.lead.entity.PersonData
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

/**
 * Extension function to convert LeadCreateRequest to Lead entity.
 * This keeps the mapping logic close to the DTO and prevents service classes from becoming huge.
 */
fun LeadCreateRequest.toLead(personData: List<PersonData>): Lead {
    return Lead(
        requestedAmountRange = requestedAmountRange,
        purpose = purpose,
        productCode = productCode,
        pipelineKey = "HOME_LOAN",
        currentStage = "APPLICATION_RECEIVED",
        sourcingChannel = sourcingChannel,
        preliminaryInformation = preliminaryInformation?.let { mapOf("data" to it) },
        extData = extData,
        personData = personData
    )
}
