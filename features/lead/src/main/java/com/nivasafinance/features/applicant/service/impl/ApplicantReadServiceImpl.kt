package com.nivasafinance.features.applicant.service.impl

import base.BaseNavigatorService
import com.nivasafinance.features.applicant.dto.ApplicantData
import com.nivasafinance.features.applicant.exception.ApplicantNotFoundException
import com.nivasafinance.features.applicant.repository.ApplicantRepository
import com.nivasafinance.features.applicant.service.ApplicantReadService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ApplicantReadServiceImpl(
    private val applicantRepository: ApplicantRepository
) : ApplicantReadService, BaseNavigatorService() {

    @Transactional(readOnly = true)
    override fun getApplicant(applicantId: UUID): ApplicantData {
        val applicant = applicantRepository.findById(applicantId)
            .orElseThrow { ApplicantNotFoundException(applicantId, messageSource) }

        return ApplicantData(
            id = applicant.id,
            personId = applicant.personId,
            leadId = applicant.leadId,
            applicantType = applicant.applicantType,
            relationshipToPrimary = applicant.relationshipToPrimary,
            status = applicant.status
        )
    }

    @Transactional(readOnly = true)
    override fun getApplicantsByLeadId(leadId: UUID): List<ApplicantData> {
        val applicants = applicantRepository.findByLeadId(leadId)
        return applicants.map { applicant ->
            ApplicantData(
                id = applicant.id,
                personId = applicant.personId,
                leadId = applicant.leadId,
                applicantType = applicant.applicantType,
                relationshipToPrimary = applicant.relationshipToPrimary,
                status = applicant.status
            )
        }
    }

    @Transactional(readOnly = true)
    override fun getApplicantsByPersonId(personId: UUID): List<ApplicantData> {
        val applicants = applicantRepository.findByPersonId(personId)
        return applicants.map { applicant ->
            ApplicantData(
                id = applicant.id,
                personId = applicant.personId,
                leadId = applicant.leadId,
                applicantType = applicant.applicantType,
                relationshipToPrimary = applicant.relationshipToPrimary,
                status = applicant.status
            )
        }
    }
}
