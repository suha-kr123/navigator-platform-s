package com.nivasafinance.features.wrapper.service.impl

import base.BaseNavigatorService
import com.nivasafinance.features.wrapper.dto.ApplicantWrapperRequest
import com.nivasafinance.features.wrapper.dto.ApplicantWrapperResponse
import com.nivasafinance.features.wrapper.service.ApplicantWrapperReadService
import com.nivasafinance.features.wrapper.service.ApplicantWrapperService
import com.nivasafinance.features.wrapper.service.ApplicantWrapperWriteService
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ApplicantWrapperServiceImpl(
    private val applicantWrapperWriteService: ApplicantWrapperWriteService,
    private val applicantWrapperReadService: ApplicantWrapperReadService
) : ApplicantWrapperService, BaseNavigatorService() {

    companion object {
        private const val CACHE_NAME = "applicant_wrapper"
    }

    @Transactional
    @CacheEvict(value = [CACHE_NAME], allEntries = true)
    override fun createApplicant(request: ApplicantWrapperRequest): ApplicantWrapperResponse {
        return applicantWrapperWriteService.createApplicant(request)
    }

    @Cacheable(value = [CACHE_NAME], key = "#applicantId")
    override fun getApplicant(applicantId: UUID): ApplicantWrapperResponse {
        return applicantWrapperReadService.getApplicant(applicantId)
    }
}
