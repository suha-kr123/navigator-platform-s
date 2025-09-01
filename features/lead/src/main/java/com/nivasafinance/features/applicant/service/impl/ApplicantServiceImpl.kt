package com.nivasafinance.features.applicant.service.impl

import base.BaseNavigatorService
import com.nivasafinance.features.applicant.dto.ApplicantCreateRequest
import com.nivasafinance.features.applicant.dto.ApplicantData
import com.nivasafinance.features.applicant.dto.ApplicantResponse
import com.nivasafinance.features.applicant.dto.ApplicantUpdateRequest
import com.nivasafinance.features.applicant.service.ApplicantReadService
import com.nivasafinance.features.applicant.service.ApplicantService
import com.nivasafinance.features.applicant.service.ApplicantWriteService
import com.nivasafinance.features.person.service.PersonReadService
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ApplicantServiceImpl(
    private val applicantReadService: ApplicantReadService,
    private val applicantWriteService: ApplicantWriteService,
    private val personReadService: PersonReadService,
    private val cacheManager: CacheManager
) : ApplicantService, BaseNavigatorService() {

    companion object {
        private const val CACHE_NAME = "applicant"
    }

    @Cacheable(value = [CACHE_NAME], key = "#applicantId")
    override fun getApplicant(applicantId: UUID): ApplicantResponse {
        val applicantData = applicantReadService.getApplicant(applicantId)
        return mapDataToResponse(applicantData)
    }

    @Cacheable(value = [CACHE_NAME], key = "'lead_' + #leadId")
    override fun getApplicantsByLeadId(leadId: UUID): List<ApplicantResponse> {
        val applicantsData = applicantReadService.getApplicantsByLeadId(leadId)
        return applicantsData.map { mapDataToResponse(it) }
    }

    @Cacheable(value = [CACHE_NAME], key = "'mobile_' + #mobileNumber")
    override fun getApplicantsByMobileNumber(mobileNumber: String): List<ApplicantResponse> {
        val personData = personReadService.getPersonByMobileNo(mobileNumber)
        val applicantsData = applicantReadService.getApplicantsByPersonId(personData.id!!)
        return applicantsData.map { mapDataToResponse(it) }
    }

    @Transactional
    @Caching(
        put = [CachePut(value = [CACHE_NAME], key = "#result.id")],
        evict = [CacheEvict(value = [CACHE_NAME], key = "'lead_' + #request.leadId")]
    )
    override fun createApplicant(request: ApplicantCreateRequest): ApplicantResponse {
        val applicantId = applicantWriteService.createApplicant(request)
        val applicantData = applicantReadService.getApplicant(applicantId)
        return mapDataToResponse(applicantData)
    }

    @Transactional
    @Caching(
        evict = [
            CacheEvict(value = [CACHE_NAME], key = "#applicantId"),
            CacheEvict(value = [CACHE_NAME], key = "'lead_' + #leadId")
        ]
    )
    override fun updateApplicant(applicantId: UUID, request: ApplicantUpdateRequest): ApplicantResponse {
        applicantWriteService.updateApplicant(applicantId, request)
        val applicantData = applicantReadService.getApplicant(applicantId)
        return mapDataToResponse(applicantData)
    }

    @Transactional
    @CacheEvict(value = [CACHE_NAME], key = "#applicantId")
    override fun deleteApplicant(applicantId: UUID) {
        val applicantData = applicantReadService.getApplicant(applicantId)
        applicantWriteService.deleteApplicant(applicantId)
        cacheManager.getCache(CACHE_NAME)?.evict("lead_${applicantData.leadId}")
    }

    private fun mapDataToResponse(applicantData: ApplicantData): ApplicantResponse {
        return ApplicantResponse(
            id = applicantData.id,
            personId = applicantData.personId,
            leadId = applicantData.leadId,
            applicantType = applicantData.applicantType,
            relationshipToPrimary = applicantData.relationshipToPrimary,
            status = applicantData.status
        )
    }
}
