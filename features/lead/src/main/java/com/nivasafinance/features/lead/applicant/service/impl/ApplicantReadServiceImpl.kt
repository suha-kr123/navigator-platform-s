package com.nivasafinance.features.lead.applicant.service.impl

import base.BaseNavigatorService
import com.nivasafinance.features.lead.applicant.dto.ApplicantResponse
import com.nivasafinance.features.lead.applicant.exception.ApplicantNotFoundException
import com.nivasafinance.features.lead.applicant.repository.ApplicantRepository
import com.nivasafinance.features.lead.applicant.service.ApplicantReadService
import com.nivasafinance.features.person.repository.PersonRepository
import data.IdentifierResponse
import data.IdentifierType
import exception.ResourceNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ApplicantReadServiceImpl(
    private val applicantRepository: ApplicantRepository,
    private val personRepository: PersonRepository,
) : ApplicantReadService, BaseNavigatorService() {

    @Transactional(readOnly = true)
    override fun getApplicantById(id: UUID): ApplicantResponse {
        val applicant = applicantRepository.findById(id)
            .orElseThrow { ApplicantNotFoundException(id, messageSource) }

        return ApplicantResponse(
            id = id,
            personId = applicant.personId,
            leadId = applicant.leadId,
            status = applicant.status.toString()
        )
    }

    @Transactional(readOnly = true)
    override fun getByIdentifierId(applicantId: UUID, identifierId: UUID): IdentifierResponse {
        val applicant = applicantRepository.findById(applicantId)
            .orElseThrow { ApplicantNotFoundException(applicantId, messageSource) }
        val person = personRepository.findById(applicant.personId)
            .orElseThrow { ResourceNotFoundException("Person not found for applicant: $applicantId") }
        val identifier = person.identifiers?.find { it.id == identifierId.toString() }
            ?: throw ResourceNotFoundException("Identifier not found with id: $identifierId")

        return IdentifierResponse(
            id = identifierId,
            identifier = identifier.identifier,
            type = IdentifierType.valueOf(identifier.type.name)
        )
    }
}
