package com.nivasafinance.features.advisor.service.impl

import base.BaseNavigatorService
import client.PersonClient
import com.nivasafinance.features.advisor.dto.AdvisorCreateRequest
import com.nivasafinance.features.advisor.dto.AdvisorResponse
import com.nivasafinance.features.advisor.dto.AdvisorUpdateRequest
import com.nivasafinance.features.advisor.dto.EmailAddress
import com.nivasafinance.features.advisor.dto.MobileNumber
import com.nivasafinance.features.advisor.dto.PersonResponse
import com.nivasafinance.features.advisor.entity.Advisor
import com.nivasafinance.features.advisor.enum.AdvisorStatus
import com.nivasafinance.features.advisor.exception.AdvisorConflictException
import com.nivasafinance.features.advisor.exception.AdvisorNotFoundException
import com.nivasafinance.features.advisor.repository.AdvisorRepository
import com.nivasafinance.features.advisor.service.AdvisorService
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@CacheConfig(cacheManager = "advisorCacheManager")
class AdvisorServiceImpl(
    private val advisorRepository: AdvisorRepository,
    private val personClient: PersonClient
) : AdvisorService, BaseNavigatorService() {

    companion object {
        private const val CACHE_NAME = "advisor"
    }

    @Cacheable(cacheNames = [CACHE_NAME], key = "#id")
    override fun getAdvisor(id: UUID): AdvisorResponse {
        val advisor = advisorRepository.findById(id)
            .orElseThrow { AdvisorNotFoundException(id, messageSource) }
        return buildAdvisorResponseFromEntity(advisor)
    }

    @Cacheable(cacheNames = [CACHE_NAME], key = "'mobileNumber' + #mobileNo")
    override fun getAdvisorByMobileNo(mobileNo: String): AdvisorResponse {
        val advisor = advisorRepository.findByPrimaryMobileNo(mobileNo)
            ?: throw AdvisorNotFoundException(UUID.randomUUID(), messageSource) // We don't have the actual ID here
        return buildAdvisorResponseFromEntity(advisor)
    }

    @Transactional
    @Caching(
        put = [CachePut(cacheNames = [CACHE_NAME], key = "#result.id")],
        evict = [CacheEvict(cacheNames = [CACHE_NAME], allEntries = true)]
    )
    override fun createAdvisor(request: AdvisorCreateRequest): AdvisorResponse {
        val personId = request.personId ?: error("Person ID is required")
        personClient.getPerson(personId).get()

        val existingAdvisor = advisorRepository.findByPersonId(personId)
        if (existingAdvisor != null) {
            throw AdvisorConflictException(personId, messageSource)
        }

        val advisor = Advisor(
            personId = personId,
            advisorCode = request.advisorCode,
            isEmployee = request.isEmployee,
            status = AdvisorStatus.CREATED,
            remarks = request.remarks,
            rejectionReason = request.rejectionReason,
            advisorFeedback = request.advisorFeedback,
            welcomeKitSent = request.welcomeKitSent,
            attendedAdvisorMeeting = request.attendedAdvisorMeeting,
            extData = request.extData
        )

        val savedAdvisor = advisorRepository.save(advisor)
        return buildAdvisorResponseFromEntity(savedAdvisor)
    }

    @Transactional
    @Caching(
        put = [CachePut(cacheNames = [CACHE_NAME], key = "#id")],
        evict = [CacheEvict(cacheNames = [CACHE_NAME], allEntries = true)]
    )
    override fun updateAdvisor(id: UUID, request: AdvisorUpdateRequest): AdvisorResponse {
        val existingAdvisor = advisorRepository.findById(id).orElseThrow {
            AdvisorNotFoundException(id, messageSource)
        }

        val updatedAdvisor = Advisor(
            id = existingAdvisor.id,
            personId = existingAdvisor.personId,
            advisorCode = request.advisorCode ?: existingAdvisor.advisorCode,
            isEmployee = request.isEmployee ?: existingAdvisor.isEmployee,
            status = request.status ?: existingAdvisor.status,
            isExperiencedDsa = existingAdvisor.isExperiencedDsa,
            remarks = request.remarks,
            rejectionReason = request.rejectionReason,
            advisorFeedback = request.advisorFeedback,
            welcomeKitSent = request.welcomeKitSent ?: existingAdvisor.welcomeKitSent,
            attendedAdvisorMeeting = request.attendedAdvisorMeeting ?: existingAdvisor.attendedAdvisorMeeting,
            extData = request.extData ?: existingAdvisor.extData
        )

        val savedAdvisor = advisorRepository.save(updatedAdvisor)
        return buildAdvisorResponseFromEntity(savedAdvisor)
    }

    @Transactional
    @Caching(
        evict = [
            CacheEvict(cacheNames = [CACHE_NAME], key = "#id"),
            CacheEvict(cacheNames = [CACHE_NAME], allEntries = true)
        ]
    )
    override fun deleteAdvisor(id: UUID) {
        val advisor = advisorRepository.findById(id).orElseThrow {
            AdvisorNotFoundException(id, messageSource)
        }
        advisorRepository.deleteById(advisor.id ?: error("Advisor ID is null"))
    }

    private fun buildAdvisorResponseFromEntity(advisor: Advisor): AdvisorResponse {
        val eventPersonInfo = personClient.getPerson(advisor.personId).get()
        val personDetails = PersonResponse(
            id = eventPersonInfo.id,
            firstName = eventPersonInfo.firstName,
            lastName = eventPersonInfo.lastName,
            mobileNumbers = eventPersonInfo.mobileNumbers.map {
                MobileNumber(
                    number = it.number,
                    isPrimary = it.isPrimary,
                    isVerified = it.isVerified
                )
            },
            emailAddresses = eventPersonInfo.emailAddresses.map {
                EmailAddress(
                    email = it.email,
                    isPrimary = it.isPrimary,
                    isVerified = it.isVerified
                )
            },
            dateOfBirth = eventPersonInfo.dateOfBirth,
            gender = eventPersonInfo.gender,
            status = eventPersonInfo.status
        )

        return AdvisorResponse(
            id = advisor.id ?: error("Advisor ID is null"),
            personId = advisor.personId,
            advisorCode = advisor.advisorCode,
            isEmployee = advisor.isEmployee,
            status = advisor.status,
            remarks = advisor.remarks,
            rejectionReason = advisor.rejectionReason,
            advisorFeedback = advisor.advisorFeedback,
            welcomeKitSent = advisor.welcomeKitSent,
            attendedAdvisorMeeting = advisor.attendedAdvisorMeeting,
            extData = advisor.extData,
            personalDetails = personDetails
        )
    }
}
