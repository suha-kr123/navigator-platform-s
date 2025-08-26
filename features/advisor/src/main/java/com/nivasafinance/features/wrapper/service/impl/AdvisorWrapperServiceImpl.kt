package com.nivasafinance.features.wrapper.service.impl

import base.BaseNavigatorService
import com.nivasafinance.features.wrapper.dto.AdvisorWrapperRequest
import com.nivasafinance.features.wrapper.dto.AdvisorWrapperResponse
import com.nivasafinance.features.wrapper.dto.CreateLeadForAdvisorRequest
import com.nivasafinance.features.wrapper.service.AdvisorWrapperReadService
import com.nivasafinance.features.wrapper.service.AdvisorWrapperService
import com.nivasafinance.features.wrapper.service.AdvisorWrapperWriteService
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AdvisorWrapperServiceImpl(
    private val advisorWrapperReadService: AdvisorWrapperReadService,
    private val advisorWrapperWriteService: AdvisorWrapperWriteService
) : AdvisorWrapperService, BaseNavigatorService() {

    companion object {
        private const val CACHE_NAME = "advisor_wrapper"
    }

    @Transactional
    @CacheEvict(value = [CACHE_NAME], allEntries = true)
    override fun createAdvisor(request: AdvisorWrapperRequest): AdvisorWrapperResponse {
        return advisorWrapperWriteService.createAdvisor(request)
    }

    @Cacheable(value = [CACHE_NAME], key = "#advisorId")
    override fun getAdvisor(advisorId: UUID): AdvisorWrapperResponse {
        return advisorWrapperReadService.getAdvisor(advisorId)
    }

    @Transactional
    @CacheEvict(value = [CACHE_NAME], allEntries = true)
    override fun createLeadForAdvisor(advisorId: UUID, request: CreateLeadForAdvisorRequest): AdvisorWrapperResponse {
        return advisorWrapperWriteService.createLeadForAdvisor(advisorId, request)
    }
}
