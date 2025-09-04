package com.nivasafinance.features.wrapper.dto

import com.nivasafinance.features.applicant.enum.ApplicantStatus
import com.nivasafinance.features.applicant.enum.ApplicantType
import com.nivasafinance.features.applicant.enum.RelationshipToPrimary
import com.nivasafinance.features.lead.enum.LeadStage
import com.nivasafinance.features.lead.enum.LeadStatus
import com.nivasafinance.features.lead.enum.SourcingChannel
import java.math.BigDecimal

data class ApplicantWrapperRequest(
    val personData: PersonData,
    val leadData: LeadData? = null,
    val applicantData: ApplicantData? = null
) {
    data class PersonData(
        val firstName: String? = null,
        val middleName: String? = null,
        val lastName: String? = null,
        val mobileNumbers: List<MobileNumberDetails> = emptyList(),
        val email: String? = null,
        val dateOfBirth: String? = null,
        val gender: String? = null,
        val addresses: List<AddressData> = emptyList(),
        val identifiers: List<IdentifierData> = emptyList()
    ) {
        data class MobileNumberDetails(
            val number: String? = null,
            val isPrimary: Boolean? = null
        )

        data class AddressData(
            val addressOne: String? = null,
            val addressTwo: String? = null,
            val landmark: String? = null,
            val district: String? = null,
            val state: String? = null,
            val pincode: String,
            val addressSource: String? = null,
            val addressType: String
        )

        data class IdentifierData(
            val identifier: String,
            val type: String
        )
    }

    data class LeadData(
        val requestedAmount: BigDecimal?,
        val purpose: String? = null,
        val productCode: String? = null,
        val sourcingChannel: SourcingChannel? = null,
        val preliminaryInformation: LeadPreliminaryInformation? = null,
        val leadContacts: LeadContacts? = null,
        val stage: LeadStage? = null,
        val status: LeadStatus? = null,
        val extData: Map<String, Any>? = null
    ) {
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
    }

    data class ApplicantData(
        val applicantType: ApplicantType = ApplicantType.PRIMARY,
        val relationshipToPrimary: RelationshipToPrimary = RelationshipToPrimary.SELF,
        val status: ApplicantStatus = ApplicantStatus.NEEDS_TO_BE_REVIEWED
    )
}
