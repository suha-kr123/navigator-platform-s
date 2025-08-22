package com.nivasafinance.features.lead.lead.service.impl

import base.BaseNavigatorService
import com.nivasafinance.features.lead.applicant.service.ApplicantWriteService
import com.nivasafinance.features.lead.lead.dto.LeadCreateRequest
import com.nivasafinance.features.lead.lead.dto.LeadPatchRequest
import com.nivasafinance.features.lead.lead.dto.LeadResponse
import com.nivasafinance.features.lead.lead.entity.Lead
import com.nivasafinance.features.lead.lead.enum.LeadStage
import com.nivasafinance.features.lead.lead.enum.LeadStatus
import com.nivasafinance.features.lead.lead.exception.LeadNotFoundException
import com.nivasafinance.features.lead.lead.repository.LeadRepository
import com.nivasafinance.features.lead.lead.service.LeadWriteService
import com.nivasafinance.features.person.service.PersonWriteService
import data.Identifier
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class LeadWriteServiceImpl(
    private val leadRepository: LeadRepository,
    private val personWriteService: PersonWriteService,
    private val applicantWriteService: ApplicantWriteService,
) : LeadWriteService, BaseNavigatorService() {

    @Transactional
    override fun createLead(request: LeadCreateRequest): LeadResponse {
        val person = personWriteService.savePerson(request.applicantDetails.personalDetails)
        val personId = person.id ?: error("Person ID is null")
        val lead = Lead(
            id = request.id,
            requestedAmount = request.requestedAmount,
            purpose = request.purpose,
            productCode = request.productCode,
            status = request.status ?: LeadStatus.ACTIVE,
            stage = request.stage ?: LeadStage.INQUIRY,
            preimerlyInformation = request.preimerlyInformation,
            leadContacts = request.leadContacts,
            sourcingChannel = request.sourcingChannel
        )

        val savedLead = leadRepository.save(lead)
        applicantWriteService.createApplicant(
            personId,
            savedLead.id,
            request.applicantDetails
        )
        return LeadResponse(
            id = savedLead.id,
            stage = savedLead.stage.toString(),
            status = savedLead.status.toString()
        )
    }

    @Transactional
    override fun patchLead(id: UUID, request: LeadPatchRequest) {
        val lead = leadRepository.findById(id)
            .orElseThrow { LeadNotFoundException(id, messageSource) }

        request.requestedAmount?.let { lead.requestedAmount = it }
        request.purpose?.let { lead.purpose = it }
        request.productCode?.let { lead.productCode = it }
        request.sourcingChannel?.let { lead.sourcingChannel = it }
        request.preimerlyInformation?.let { info ->
            lead.preimerlyInformation = info
        }
        request.stage?.let { lead.stage = it }
        request.status?.let { lead.status = it }
        request.leadContacts?.let { lead.leadContacts = it }

        leadRepository.save(lead)
    }

    @Transactional
    override fun saveIdentifier(id: UUID, applicantId: UUID, addIdentifier: Identifier) {
        val lead = leadRepository.findById(id)
            .orElseThrow { LeadNotFoundException(id, messageSource) }
        applicantWriteService.addIdentifier(applicantId, addIdentifier)
    }

    @Transactional
    override fun updateIdentifier(id: UUID, applicantId: UUID, identifierId: UUID, addIdentifier: Identifier) {
        val lead = leadRepository.findById(id)
            .orElseThrow { LeadNotFoundException(id, messageSource) }
        applicantWriteService.updateIdentifier(applicantId, identifierId, addIdentifier)
    }
}
