package com.nivasafinance.features

import com.nivasafinance.features.applicant.dto.ApplicantCreateRequest
import com.nivasafinance.features.applicant.dto.ApplicantResponse
import com.nivasafinance.features.applicant.entity.Applicant
import com.nivasafinance.features.applicant.enum.ApplicantStatus
import com.nivasafinance.features.applicant.enum.ApplicantType
import com.nivasafinance.features.applicant.enum.RelationshipToPrimary
import com.nivasafinance.features.lead.dto.LeadCreateRequest
import com.nivasafinance.features.lead.dto.LeadResponse
import com.nivasafinance.features.lead.dto.LeadUpdateRequest
import com.nivasafinance.features.lead.entity.Lead
import com.nivasafinance.features.lead.enum.LeadStage
import com.nivasafinance.features.lead.enum.LeadStatus
import com.nivasafinance.features.lead.enum.SourcingChannel
import java.math.BigDecimal
import java.util.UUID

object TestUtils {

    fun createTestLeadCreateRequest(
        requestedAmount: BigDecimal = BigDecimal("500000"),
        purpose: String = "Home Construction",
        productCode: String = "HL001",
        sourcingChannel: SourcingChannel = SourcingChannel.DIRECT
    ): LeadCreateRequest {
        return LeadCreateRequest(
            requestedAmount = requestedAmount,
            purpose = purpose,
            productCode = productCode,
            preliminaryInformation = null,
            leadContacts = null,
            sourcingChannel = sourcingChannel
        )
    }

    fun createTestLeadResponse(
        id: UUID = UUID.randomUUID(),
        requestedAmount: BigDecimal = BigDecimal("500000"),
        purpose: String = "Home Construction",
        productCode: String = "HL001",
        sourcingChannel: SourcingChannel = SourcingChannel.DIRECT,
        stage: LeadStage = LeadStage.INQUIRY,
        status: LeadStatus = LeadStatus.ACTIVE
    ): LeadResponse {
        return LeadResponse(
            id = id,
            requestedAmount = requestedAmount,
            purpose = purpose,
            productCode = productCode,
            sourcingChannel = sourcingChannel,
            stage = stage,
            status = status,
            preliminaryInformation = null,
            leadContacts = null,
            extData = null
        )
    }

    fun createTestLeadUpdateRequest(
        requestedAmount: BigDecimal? = BigDecimal("600000"),
        purpose: String? = "Home Renovation",
        productCode: String? = "HL002"
    ): LeadUpdateRequest {
        return LeadUpdateRequest(
            requestedAmount = requestedAmount,
            purpose = purpose,
            productCode = productCode
        )
    }

    fun createTestLead(
        id: UUID = UUID.randomUUID(),
        requestedAmount: BigDecimal = BigDecimal("500000"),
        purpose: String = "Home Construction",
        productCode: String = "HL001",
        sourcingChannel: SourcingChannel = SourcingChannel.DIRECT,
        stage: LeadStage = LeadStage.INQUIRY,
        status: LeadStatus = LeadStatus.ACTIVE
    ): Lead {
        return Lead(
            id = id,
            requestedAmount = requestedAmount,
            purpose = purpose,
            productCode = productCode,
            sourcingChannel = sourcingChannel,
            stage = stage,
            status = status,
            preliminaryInformation = null,
            leadContacts = null,
            extData = null
        )
    }

    fun createTestApplicantCreateRequest(
        leadId: UUID = UUID.randomUUID(),
        personId: UUID = UUID.randomUUID(),
        applicantType: ApplicantType = ApplicantType.PRIMARY,
        relationshipToPrimary: RelationshipToPrimary = RelationshipToPrimary.SELF,
        status: ApplicantStatus = ApplicantStatus.NEEDS_TO_BE_REVIEWED
    ): ApplicantCreateRequest {
        return ApplicantCreateRequest(
            leadId = leadId,
            personId = personId,
            applicantType = applicantType,
            relationshipToPrimary = relationshipToPrimary,
            status = status
        )
    }

    fun createTestApplicantResponse(
        id: UUID = UUID.randomUUID(),
        personId: UUID = UUID.randomUUID(),
        leadId: UUID = UUID.randomUUID(),
        applicantType: ApplicantType = ApplicantType.PRIMARY,
        relationshipToPrimary: RelationshipToPrimary = RelationshipToPrimary.SELF,
        status: ApplicantStatus = ApplicantStatus.NEEDS_TO_BE_REVIEWED
    ): ApplicantResponse {
        return ApplicantResponse(
            id = id,
            personId = personId,
            leadId = leadId,
            applicantType = applicantType,
            relationshipToPrimary = relationshipToPrimary,
            status = status
        )
    }

    fun createTestApplicant(
        id: UUID = UUID.randomUUID(),
        personId: UUID = UUID.randomUUID(),
        leadId: UUID = UUID.randomUUID(),
        applicantType: ApplicantType = ApplicantType.PRIMARY,
        relationshipToPrimary: RelationshipToPrimary = RelationshipToPrimary.SELF,
        status: ApplicantStatus = ApplicantStatus.NEEDS_TO_BE_REVIEWED
    ): Applicant {
        return Applicant(
            id = id,
            personId = personId,
            leadId = leadId,
            applicantType = applicantType,
            relationshipToPrimary = relationshipToPrimary,
            status = status
        )
    }
}
