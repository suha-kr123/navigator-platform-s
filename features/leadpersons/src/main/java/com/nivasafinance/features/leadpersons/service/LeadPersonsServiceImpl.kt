package com.nivasafinance.features.leadpersons.service

import base.model.PaginatedResponse
import base.model.PaginationInfo
import base.model.PaginationRequest
import com.nivasafinance.features.leadpersons.dto.LeadPersonRequest
import com.nivasafinance.features.leadpersons.dto.LeadPersonResponse
import com.nivasafinance.features.leadpersons.entity.LeadPersons
import com.nivasafinance.features.leadpersons.exception.LeadPersonsExceptionFactory
import com.nivasafinance.features.leadpersons.repository.LeadPersonsRepositoryWrapper
import org.springframework.context.MessageSource
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class LeadPersonsServiceImpl(
    private val leadPersonsRepositoryWrapper: LeadPersonsRepositoryWrapper,
    private val messageSource: MessageSource
) : LeadPersonsService {

    override fun createLeadPerson(leadPersonRequest: LeadPersonRequest): LeadPersonResponse {
        val exists = leadPersonsRepositoryWrapper.existsByLeadIdAndPersonIdWithException(
            leadPersonRequest.leadId, leadPersonRequest.personId
        )
        if (exists) {
            throw LeadPersonsExceptionFactory.alreadyExists(
                leadPersonRequest.leadId, leadPersonRequest.personId, messageSource
            )
        }

        val leadPerson = LeadPersons(
            leadId = leadPersonRequest.leadId,
            personId = leadPersonRequest.personId,
            isApplicant = leadPersonRequest.isApplicant,
            applicantType = leadPersonRequest.applicantType,
            relationshipToPrimary = leadPersonRequest.relationshipToPrimary,
            tags = leadPersonRequest.tags,
            verificationStatus = leadPersonRequest.verificationStatus,
            verificationNotes = leadPersonRequest.verificationNotes,
            extData = leadPersonRequest.extData
        )

        val savedLeadPerson = leadPersonsRepositoryWrapper.saveWithException(leadPerson)
        return toLeadPersonResponse(savedLeadPerson)
    }

    override fun getLeadPersonById(id: UUID): LeadPersonResponse {
        val leadPerson = leadPersonsRepositoryWrapper.findByIdWithException(id)
        return toLeadPersonResponse(leadPerson)
    }

    override fun getAllLeadPersons(paginationRequest: PaginationRequest): PaginatedResponse<LeadPersonResponse> {
        val pageable = PageRequest.of(
            paginationRequest.offset / paginationRequest.limit,
            paginationRequest.limit
        )
        val leadPersonPage = leadPersonsRepositoryWrapper.findAllWithException(pageable)
        val leadPersonResponses = leadPersonPage.content.map { toLeadPersonResponse(it) }

        val totalPages = if (leadPersonPage.totalElements == 0L) 0 else ((leadPersonPage.totalElements - 1) / paginationRequest.limit + 1).toInt()
        val currentPage = paginationRequest.offset / paginationRequest.limit

        return PaginatedResponse(
            content = leadPersonResponses,
            pagination = PaginationInfo(
                offset = paginationRequest.offset,
                limit = paginationRequest.limit,
                totalElements = leadPersonPage.totalElements,
                totalPages = totalPages,
                currentPage = currentPage,
                hasNext = currentPage < totalPages - 1,
                hasPrevious = currentPage > 0
            )
        )
    }

    override fun getLeadPersonsByLeadId(leadId: UUID): List<LeadPersonResponse> {
        val leadPersons = leadPersonsRepositoryWrapper.findByLeadIdWithException(leadId)
        return leadPersons.map { toLeadPersonResponse(it) }
    }

    override fun getLeadPersonsByPersonId(personId: UUID): List<LeadPersonResponse> {
        val leadPersons = leadPersonsRepositoryWrapper.findByPersonIdWithException(personId)
        return leadPersons.map { toLeadPersonResponse(it) }
    }

    override fun updateLeadPerson(id: UUID, leadPersonRequest: LeadPersonRequest): LeadPersonResponse {
        val existingLeadPerson = leadPersonsRepositoryWrapper.findByIdWithException(id)
        existingLeadPerson.leadId = leadPersonRequest.leadId
        existingLeadPerson.personId = leadPersonRequest.personId
        existingLeadPerson.isApplicant = leadPersonRequest.isApplicant
        existingLeadPerson.applicantType = leadPersonRequest.applicantType
        existingLeadPerson.relationshipToPrimary = leadPersonRequest.relationshipToPrimary
        existingLeadPerson.tags = leadPersonRequest.tags
        existingLeadPerson.verificationStatus = leadPersonRequest.verificationStatus
        existingLeadPerson.verificationNotes = leadPersonRequest.verificationNotes
        existingLeadPerson.extData = leadPersonRequest.extData
        val savedLeadPerson = leadPersonsRepositoryWrapper.saveWithException(existingLeadPerson)
        return toLeadPersonResponse(savedLeadPerson)
    }

    override fun deleteLeadPerson(id: UUID) {
        leadPersonsRepositoryWrapper.deleteWithException(id)
    }

    private fun toLeadPersonResponse(leadPerson: LeadPersons): LeadPersonResponse {
        return LeadPersonResponse(
            id = leadPerson.id!!,
            leadId = leadPerson.leadId!!,
            personId = leadPerson.personId!!,
            isApplicant = leadPerson.isApplicant!!,
            applicantType = leadPerson.applicantType!!,
            relationshipToPrimary = leadPerson.relationshipToPrimary!!,
            tags = leadPerson.tags,
            verificationStatus = leadPerson.verificationStatus!!,
            verificationNotes = leadPerson.verificationNotes,
            extData = leadPerson.extData,
            createdAt = leadPerson.createdAt!!,
            createdBy = leadPerson.createdBy,
            updatedAt = leadPerson.updatedAt!!,
            updatedBy = leadPerson.updatedBy
        )
    }
}
