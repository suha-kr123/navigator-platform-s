package com.nivasafinance.features.advisor.service.impl

import base.BaseNavigatorService
import com.nivasafinance.features.advisor.dto.AdvisorCreateRequest
import com.nivasafinance.features.advisor.dto.AdvisorResponse
import com.nivasafinance.features.advisor.dto.AdvisorUpdateRequest
import com.nivasafinance.features.advisor.service.AdvisorReadService
import com.nivasafinance.features.advisor.service.AdvisorService
import com.nivasafinance.features.advisor.service.AdvisorWriteService
import com.nivasafinance.features.person.service.PersonService
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AdvisorServiceImpl(
    private val advisorReadService: AdvisorReadService,
    private val advisorWriteService: AdvisorWriteService,
    private val personService: PersonService
) : AdvisorService, BaseNavigatorService() {

    companion object {
        private const val CACHE_NAME = "advisor"
    }

    @Cacheable(cacheNames = [CACHE_NAME], key = "#id")
    override fun getAdvisor(id: UUID): AdvisorResponse {
        val advisorData = advisorReadService.getAdvisorData(id)
        return buildAdvisorResponse(advisorData)
    }

    @Cacheable(cacheNames = [CACHE_NAME], key = "'mobileNumber' + #mobileNo")
    override fun getAdvisorByMobileNo(mobileNo: String): AdvisorResponse {
        val advisorData = advisorReadService.getAdvisorDataByMobileNo(mobileNo)
        return buildAdvisorResponse(advisorData)
    }

    @Transactional
    @Caching(
        put = [CachePut(cacheNames = [CACHE_NAME], key = "#result.id")],
        evict = [CacheEvict(cacheNames = [CACHE_NAME], allEntries = true)]
    )
    override fun createAdvisor(request: AdvisorCreateRequest): AdvisorResponse {
        val advisorData = advisorWriteService.createAdvisorData(request)
        return buildAdvisorResponse(advisorData)
    }

    @Transactional
    @Caching(
        put = [CachePut(cacheNames = [CACHE_NAME], key = "#id")],
        evict = [CacheEvict(cacheNames = [CACHE_NAME], allEntries = true)]
    )
    override fun updateAdvisor(id: UUID, request: AdvisorUpdateRequest): AdvisorResponse {
        val advisorData = advisorWriteService.updateAdvisorData(id, request)
        return buildAdvisorResponse(advisorData)
    }

    @Transactional
    @Caching(
        evict = [
            CacheEvict(cacheNames = [CACHE_NAME], key = "#id"),
            CacheEvict(cacheNames = [CACHE_NAME], allEntries = true)
        ]
    )
    override fun deleteAdvisor(id: UUID) {
        advisorWriteService.deleteAdvisor(id)
    }

    private fun buildAdvisorResponse(advisorData: com.nivasafinance.features.advisor.dto.AdvisorData): AdvisorResponse {
        val personDetails = personService.getPerson(advisorData.personId)
        return AdvisorResponse(
            id = advisorData.id!!,
            personId = advisorData.personId,
            advisorCode = advisorData.advisorCode,
            isEmployee = advisorData.isEmployee,
            status = advisorData.status,
            remarks = advisorData.remarks,
            rejectionReason = advisorData.rejectionReason,
            advisorFeedback = advisorData.advisorFeedback,
            welcomeKitSent = advisorData.welcomeKitSent,
            attendedAdvisorMeeting = advisorData.attendedAdvisorMeeting,
            extData = advisorData.extData,
            personalDetails = personDetails
        )
    }
}
