package com.nivasafinance.features.advisor.service

import base.model.PaginatedResponse
import base.model.PaginationInfo
import base.model.PaginationRequest
import com.nivasafinance.features.advisor.dto.AdvisorRequest
import com.nivasafinance.features.advisor.dto.AdvisorResponse
import com.nivasafinance.features.advisor.dto.AdvisorUpdateRequest
import com.nivasafinance.features.advisor.entity.Advisor
import com.nivasafinance.features.advisor.repository.AdvisorRepositoryWrapper
import com.nivasafinance.features.person.dto.PersonResponse
import com.nivasafinance.features.person.service.PersonService
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class AdvisorServiceImpl(
    private val advisorRepositoryWrapper: AdvisorRepositoryWrapper,
    private val personService: PersonService
) : AdvisorService {

    override fun createAdvisor(advisorRequest: AdvisorRequest): AdvisorResponse {
        // First create the person
        val personResponse = personService.createPerson(advisorRequest.person)

        // Then create the advisor
        val advisor = Advisor(
            personId = personResponse.id,
            advisorCode = advisorRequest.advisorCode,
            verificationStatus = advisorRequest.verificationStatus,
            verificationNotes = advisorRequest.verificationNotes,
            extData = advisorRequest.extData
        )

        val savedAdvisor = advisorRepositoryWrapper.saveWithException(advisor)
        return toAdvisorResponse(savedAdvisor, personResponse)
    }

    override fun updateAdvisor(advisorId: UUID, updateAdvisorRequest: AdvisorUpdateRequest): AdvisorResponse {
        val existingAdvisor = advisorRepositoryWrapper.findByIdWithException(advisorId)

        // Update fields directly (only if provided)
        updateAdvisorRequest.advisorCode?.let { existingAdvisor.advisorCode = it }
        updateAdvisorRequest.verificationStatus?.let { existingAdvisor.verificationStatus = it }
        updateAdvisorRequest.verificationNotes?.let { existingAdvisor.verificationNotes = it }
        updateAdvisorRequest.extData?.let { existingAdvisor.extData = it }

        val savedAdvisor = advisorRepositoryWrapper.saveWithException(existingAdvisor)

        // Get the person details
        val personResponse = if (savedAdvisor.personId != null) {
            personService.getPerson(checkNotNull(savedAdvisor.personId))
        } else {
            null
        }

        return toAdvisorResponse(savedAdvisor, personResponse)
    }

    override fun getAdvisor(advisorId: UUID): AdvisorResponse {
        val advisor = advisorRepositoryWrapper.findByIdWithException(advisorId)

        // Get the person details
        val personResponse = if (advisor.personId != null) {
            personService.getPerson(checkNotNull(advisor.personId))
        } else {
            null
        }

        return toAdvisorResponse(advisor, personResponse)
    }

    override fun getAllAdvisors(paginationRequest: PaginationRequest): PaginatedResponse<AdvisorResponse> {
        val sort = Sort.by(
            if (paginationRequest.sortDirection.name == "ASC") Sort.Direction.ASC else Sort.Direction.DESC,
            paginationRequest.sortBy
        )

        val pageable = PageRequest.of(
            paginationRequest.offset / paginationRequest.limit,
            paginationRequest.limit,
            sort
        )

        val advisorPage = advisorRepositoryWrapper.findAllWithException(pageable)

        val advisorResponses = advisorPage.content.map { advisor ->
            val personResponse = advisor.personId?.let { personId ->
                runCatching { personService.getPerson(personId) }.getOrNull()
            }

            toAdvisorResponse(advisor, personResponse)
        }

        val paginationInfo = PaginationInfo(
            offset = paginationRequest.offset,
            limit = paginationRequest.limit,
            totalElements = advisorPage.totalElements,
            totalPages = advisorPage.totalPages,
            currentPage = advisorPage.number,
            hasNext = advisorPage.hasNext(),
            hasPrevious = advisorPage.hasPrevious()
        )

        return PaginatedResponse(
            content = advisorResponses,
            pagination = paginationInfo
        )
    }

    override fun deleteAdvisor(advisorId: UUID) {
        advisorRepositoryWrapper.findByIdWithException(advisorId)
        advisorRepositoryWrapper.deleteByIdWithException(advisorId)
    }

    private fun toAdvisorResponse(advisor: Advisor, personResponse: PersonResponse?): AdvisorResponse {
        return AdvisorResponse(
            id = checkNotNull(advisor.id) { "Advisor ID cannot be null" },
            personId = advisor.personId,
            person = personResponse,
            advisorCode = advisor.advisorCode,
            verificationStatus = advisor.verificationStatus,
            verificationNotes = advisor.verificationNotes,
            extData = advisor.extData,
            createdAt = advisor.createdAt ?: java.time.LocalDateTime.now(),
            createdBy = advisor.createdBy,
            updatedAt = advisor.updatedAt ?: java.time.LocalDateTime.now(),
            updatedBy = advisor.updatedBy
        )
    }
}
