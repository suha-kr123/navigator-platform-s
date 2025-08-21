package com.nivasafinance.features.lead.applicant.service.impl

import base.BaseNavigatorService
import com.nivasafinance.features.lead.applicant.dto.ApplicantCreateRequest
import com.nivasafinance.features.lead.applicant.dto.ApplicantPatchRequest
import com.nivasafinance.features.lead.applicant.dto.ApplicantResponse
import com.nivasafinance.features.lead.applicant.entity.Applicant
import com.nivasafinance.features.lead.applicant.exception.ApplicantNotFoundException
import com.nivasafinance.features.lead.applicant.repository.ApplicantRepository
import com.nivasafinance.features.lead.applicant.service.ApplicantWriteService
import com.nivasafinance.features.person.service.PersonWriteService
import data.Identifier
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ApplicantWriteServiceImpl(
    private val applicantRepository: ApplicantRepository,
    private val personWriteService: PersonWriteService
) : ApplicantWriteService, BaseNavigatorService() {

    @Transactional
    override fun createApplicant(
        personId: UUID,
        leadId: UUID,
        request: ApplicantCreateRequest
    ): ApplicantResponse {
        val applicant = Applicant(
            personId = personId,
            leadId = leadId,
            applicantType = request.applicantType,
            relationshipToPrimary = request.relationshipToPrimary,
            status = request.status
        )
        val savedApplicant = applicantRepository.save(applicant)
        return ApplicantResponse(
            id = savedApplicant.id,
            personId = savedApplicant.personId,
            leadId = savedApplicant.leadId,
            status = savedApplicant.status.toString()
        )
    }

    @Transactional
    override fun patchApplicant(id: UUID, request: ApplicantPatchRequest): ApplicantResponse {
        val applicant = applicantRepository.findById(id)
            .orElseThrow { ApplicantNotFoundException(id, messageSource) }
        request.applicantType?.let { applicant.applicantType = it }
        request.relationshipToPrimary?.let { applicant.relationshipToPrimary = it }
        request.status?.let { applicant.status = it }
        request.personalDetails.let { personWriteService.updatePerson(applicant.personId, it) }
        return ApplicantResponse(
            id = applicant.id,
            personId = applicant.personId,
            leadId = applicant.leadId,
            status = applicant.status.toString()
        )
    }

    @Transactional
    override fun addIdentifier(applicantId: UUID, addIdentifier: Identifier) {
        val applicant = applicantRepository.findById(applicantId)
            .orElseThrow { ApplicantNotFoundException(applicantId, messageSource) }

        personWriteService.addIdentifier(applicant.personId, addIdentifier)
    }

    @Transactional
    override fun updateIdentifier(applicantId: UUID, identifierId: UUID, addIdentifier: Identifier) {
        val applicant = applicantRepository.findById(applicantId)
            .orElseThrow { ApplicantNotFoundException(applicantId, messageSource) }

        personWriteService.updateIdentifier(applicant.personId, identifierId, addIdentifier)
    }
}
