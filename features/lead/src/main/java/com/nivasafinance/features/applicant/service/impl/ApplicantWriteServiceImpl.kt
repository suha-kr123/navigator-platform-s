package com.nivasafinance.features.applicant.service.impl

import base.BaseNavigatorService
import com.nivasafinance.features.applicant.dto.ApplicantCreateRequest
import com.nivasafinance.features.applicant.dto.ApplicantUpdateRequest
import com.nivasafinance.features.applicant.entity.Applicant
import com.nivasafinance.features.applicant.enum.ApplicantType
import com.nivasafinance.features.applicant.exception.ApplicantNotFoundException
import com.nivasafinance.features.applicant.exception.PrimaryApplicantAlreadyExistsException
import com.nivasafinance.features.applicant.repository.ApplicantRepository
import com.nivasafinance.features.applicant.service.ApplicantWriteService
import com.nivasafinance.features.lead.service.LeadService
import com.nivasafinance.features.person.service.PersonService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ApplicantWriteServiceImpl(
    private val applicantRepository: ApplicantRepository,
    private val personService: PersonService,
    private val leadService: LeadService
) : ApplicantWriteService, BaseNavigatorService() {

    @Transactional
    override fun createApplicant(request: ApplicantCreateRequest): UUID {
        // Validate that person exists
        personService.getPerson(request.personId)

        // Validate that lead exists
        leadService.getLeadById(request.leadId)

        // Validate that only one primary applicant can exist per lead
        if (request.applicantType == ApplicantType.PRIMARY) {
            val existingPrimaryApplicant = applicantRepository.findByLeadIdAndApplicantType(
                request.leadId,
                ApplicantType.PRIMARY
            )
            if (existingPrimaryApplicant.isNotEmpty()) {
                throw PrimaryApplicantAlreadyExistsException(
                    request.leadId,
                    messageSource
                )
            }
        }

        val applicant = Applicant(
            personId = request.personId,
            leadId = request.leadId,
            applicantType = request.applicantType,
            relationshipToPrimary = request.relationshipToPrimary,
            status = request.status
        )
        val savedApplicant = applicantRepository.save(applicant)
        return savedApplicant.id!!
    }

    @Transactional
    override fun updateApplicant(applicantId: UUID, request: ApplicantUpdateRequest) {
        val applicant = applicantRepository.findById(applicantId)
            .orElseThrow { ApplicantNotFoundException(applicantId, messageSource) }

        // Validate that only one primary applicant can exist per lead when updating to primary
        if (request.applicantType == ApplicantType.PRIMARY && applicant.applicantType != ApplicantType.PRIMARY) {
            val existingPrimaryApplicant = applicantRepository.findByLeadIdAndApplicantType(
                applicant.leadId,
                ApplicantType.PRIMARY
            )
            if (existingPrimaryApplicant.isNotEmpty()) {
                throw PrimaryApplicantAlreadyExistsException(
                    applicant.leadId,
                    messageSource
                )
            }
        }

        request.applicantType?.let { applicant.applicantType = it }
        request.relationshipToPrimary?.let { applicant.relationshipToPrimary = it }
        request.status?.let { applicant.status = it }

        applicantRepository.save(applicant)
    }

    @Transactional
    override fun deleteApplicant(applicantId: UUID) {
        val applicant = applicantRepository.findById(applicantId)
            .orElseThrow { ApplicantNotFoundException(applicantId, messageSource) }

        applicantRepository.delete(applicant)
    }
}
