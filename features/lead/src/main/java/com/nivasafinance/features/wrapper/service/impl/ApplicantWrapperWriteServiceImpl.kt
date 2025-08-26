package com.nivasafinance.features.wrapper.service.impl

import base.BaseNavigatorService
import com.nivasafinance.features.applicant.dto.ApplicantCreateRequest
import com.nivasafinance.features.applicant.enum.ApplicantStatus
import com.nivasafinance.features.applicant.enum.ApplicantType
import com.nivasafinance.features.applicant.enum.RelationshipToPrimary
import com.nivasafinance.features.applicant.service.ApplicantService
import com.nivasafinance.features.lead.dto.LeadCreateRequest
import com.nivasafinance.features.lead.service.LeadService
import com.nivasafinance.features.person.dto.PersonCreateRequest
import com.nivasafinance.features.person.service.PersonService
import com.nivasafinance.features.wrapper.dto.ApplicantWrapperRequest
import com.nivasafinance.features.wrapper.dto.ApplicantWrapperResponse
import com.nivasafinance.features.wrapper.service.ApplicantWrapperWriteService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ApplicantWrapperWriteServiceImpl(
    private val personService: PersonService,
    private val leadService: LeadService,
    private val applicantService: ApplicantService
) : ApplicantWrapperWriteService, BaseNavigatorService() {

    @Transactional
    override fun createApplicant(request: ApplicantWrapperRequest): ApplicantWrapperResponse {
        val person = personService.createPerson(mapPersonData(request.personData))

        val lead = if (request.leadData != null) {
            leadService.createLead(mapLeadData(request.leadData))
        } else {
            leadService.createLead(LeadCreateRequest(requestedAmount = null))
        }

        val applicant = applicantService.createApplicant(
            ApplicantCreateRequest(
                leadId = lead.id!!,
                personId = person.id!!,
                applicantType = request.applicantData?.applicantType ?: ApplicantType.PRIMARY,
                relationshipToPrimary = request.applicantData?.relationshipToPrimary ?: RelationshipToPrimary.SELF,
                status = request.applicantData?.status ?: ApplicantStatus.NEEDS_TO_BE_REVIEWED
            )
        )

        return ApplicantWrapperResponse(
            id = applicant.id!!,
            status = lead.status ?: com.nivasafinance.features.lead.enum.LeadStatus.ACTIVE,
            stage = lead.stage ?: com.nivasafinance.features.lead.enum.LeadStage.INQUIRY
        )
    }

    private fun mapPersonData(personData: ApplicantWrapperRequest.PersonData): PersonCreateRequest {
        return PersonCreateRequest(
            firstName = personData.firstName,
            middleName = personData.middleName,
            lastName = personData.lastName,
            mobileNumbers = personData.mobileNumbers.map {
                com.nivasafinance.features.person.entity.MobileNumberDetails(it.number, it.isPrimary)
            },
            email = personData.email,
            dateOfBirth = personData.dateOfBirth?.let { java.time.LocalDate.parse(it) },
            gender = personData.gender?.let { data.enums.Gender.valueOf(it) }
        )
    }

    private fun mapLeadData(leadData: ApplicantWrapperRequest.LeadData): LeadCreateRequest {
        return LeadCreateRequest(
            requestedAmount = leadData.requestedAmount,
            purpose = leadData.purpose,
            productCode = leadData.productCode,
            sourcingChannel = leadData.sourcingChannel,
            preliminaryInformation = leadData.preliminaryInformation?.let {
                com.nivasafinance.features.lead.dto.LeadPreliminaryInformation(
                    it.whenYouWantLoan,
                    it.isHouseConstructionStarted,
                    it.isEKhathaAvailable,
                    it.selfDeclaredAnnualFamilyIncome
                )
            },
            leadContacts = leadData.leadContacts?.let {
                com.nivasafinance.features.lead.dto.LeadContacts(it.name, it.number)
            },
            stage = leadData.stage,
            status = leadData.status
        )
    }
}
