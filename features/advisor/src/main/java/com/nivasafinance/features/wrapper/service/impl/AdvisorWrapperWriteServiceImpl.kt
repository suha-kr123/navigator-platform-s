package com.nivasafinance.features.wrapper.service.impl

import base.BaseNavigatorService
import com.nivasafinance.features.advisor.dto.AdvisorCreateRequest
import com.nivasafinance.features.advisor.service.AdvisorService
import com.nivasafinance.features.advisorleadmapping.service.AdvisorLeadMappingService
import com.nivasafinance.features.lead.enum.LeadStage
import com.nivasafinance.features.lead.enum.LeadStatus
import com.nivasafinance.features.lead.service.LeadService
import com.nivasafinance.features.person.dto.PersonCreateRequest
import com.nivasafinance.features.person.service.PersonService
import com.nivasafinance.features.wrapper.dto.AdvisorWrapperRequest
import com.nivasafinance.features.wrapper.dto.AdvisorWrapperResponse
import com.nivasafinance.features.wrapper.dto.CreateLeadForAdvisorRequest
import com.nivasafinance.features.wrapper.service.AdvisorWrapperWriteService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.UUID

@Service
class AdvisorWrapperWriteServiceImpl(
    private val personService: PersonService,
    private val advisorService: AdvisorService,
    private val advisorLeadMappingService: AdvisorLeadMappingService,
    private val leadService: LeadService
) : AdvisorWrapperWriteService, BaseNavigatorService() {

    @Transactional
    override fun createAdvisor(request: AdvisorWrapperRequest): AdvisorWrapperResponse {
        val personCreateRequest = PersonCreateRequest(
            firstName = request.personData.firstName,
            middleName = request.personData.middleName,
            lastName = request.personData.lastName,
            mobileNumbers = request.personData.mobileNumbers.map {
                com.nivasafinance.features.person.entity.MobileNumberDetails(it.number, it.isPrimary)
            },
            email = request.personData.email,
            dateOfBirth = request.personData.dateOfBirth?.let { LocalDate.parse(it) },
            gender = request.personData.gender?.let { data.enums.Gender.valueOf(it) }
        )

        val personResponse = personService.createPerson(personCreateRequest)

        val advisorCreateRequest = AdvisorCreateRequest(
            advisorCode = request.advisorData?.advisorCode,
            personId = personResponse.id,
            isEmployee = false,
            remarks = request.advisorData?.remarks
        )

        val advisorResponse = advisorService.createAdvisor(advisorCreateRequest)

        return AdvisorWrapperResponse(
            id = advisorResponse.id,
            name = "${personResponse.firstName.orEmpty()} ${personResponse.lastName.orEmpty()}".trim(),
            status = advisorResponse.status
                ?: com.nivasafinance.features.advisor.enum.AdvisorStatus.CREATED,
            leads = emptyList()
        )
    }

    @Transactional
    override fun createLeadForAdvisor(advisorId: UUID, request: CreateLeadForAdvisorRequest): AdvisorWrapperResponse {
        val leadCreateRequest = com.nivasafinance.features.lead.dto.LeadCreateRequest(
            requestedAmount = request.applicantWrapperRequest.leadData?.requestedAmount,
            purpose = request.applicantWrapperRequest.leadData?.purpose,
            productCode = request.applicantWrapperRequest.leadData?.productCode,
            status = request.applicantWrapperRequest.leadData?.status,
            stage = request.applicantWrapperRequest.leadData?.stage,
            preliminaryInformation = request.applicantWrapperRequest.leadData?.preliminaryInformation?.let {
                com.nivasafinance.features.lead.dto.LeadPreliminaryInformation(
                    it.whenYouWantLoan,
                    it.isHouseConstructionStarted,
                    it.isEKhathaAvailable,
                    it.selfDeclaredAnnualFamilyIncome,
                    it.preferredCallTime,
                    it.monthlyIncome
                )
            },
            leadContacts = request.applicantWrapperRequest.leadData?.leadContacts?.let {
                com.nivasafinance.features.lead.dto.LeadContacts(it.name, it.number)
            },
            sourcingChannel = request.applicantWrapperRequest.leadData?.sourcingChannel,
            extData = request.applicantWrapperRequest.leadData?.extData
        )

        val leadResponse = leadService.createLead(leadCreateRequest)

        val advisorLeadMappingCreateRequest =
            com.nivasafinance.features.advisorleadmapping.dto.AdvisorLeadMappingCreateRequest(
                remarks = request.remarks,
                extData = null
            )

        advisorLeadMappingService.createAdvisorLeadMapping(
            advisorId,
            leadResponse.id,
            advisorLeadMappingCreateRequest
        )

        val advisorResponse = advisorService.getAdvisor(advisorId)
        val leads = advisorLeadMappingService.getAllLeadsForAdvisor(advisorId)

        return AdvisorWrapperResponse(
            id = advisorResponse.id,
            name = "${advisorResponse.personalDetails.firstName.orEmpty()} " +
                "${advisorResponse.personalDetails.lastName.orEmpty()}".trim(),
            status = advisorResponse.status
                ?: com.nivasafinance.features.advisor.enum.AdvisorStatus.CREATED,
            leads = leads.map { mapping ->
                val lead = leadService.getLeadById(mapping.leadId)
                AdvisorWrapperResponse.LeadInfo(
                    leadId = mapping.leadId,
                    status = lead.status ?: LeadStatus.ACTIVE,
                    stage = lead.stage ?: LeadStage.INQUIRY
                )
            }
        )
    }
}
