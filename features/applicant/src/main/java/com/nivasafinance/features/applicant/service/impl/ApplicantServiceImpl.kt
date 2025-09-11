package com.nivasafinance.features.applicant.service.impl

import base.BaseNavigatorService
import client.PersonClient
import com.nivasafinance.features.applicant.dto.ApplicantCreateRequest
import com.nivasafinance.features.applicant.dto.ApplicantResponse
import com.nivasafinance.features.applicant.dto.ApplicantUpdateRequest
import com.nivasafinance.features.applicant.entity.Applicant
import com.nivasafinance.features.applicant.repository.ApplicantRepository
import com.nivasafinance.features.applicant.service.ApplicantService
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
    private val applicantRepository: ApplicantRepository,
    private val personClient: PersonClient,
    private val cacheManager: CacheManager
) : ApplicantService, BaseNavigatorService() {

    companion object {
        private const val CACHE_NAME = "applicant"
    }

    @Cacheable(value = [CACHE_NAME], key = "#applicantId")
    override fun getApplicant(applicantId: UUID): ApplicantResponse {
        val applicant = applicantRepository.findById(applicantId)
            .orElseThrow { RuntimeException("Applicant not found with id: $applicantId") }
        return mapEntityToResponse(applicant)
    }

    @Cacheable(value = [CACHE_NAME], key = "'lead_' + #leadId")
    override fun getApplicantsByLeadId(leadId: UUID): List<ApplicantResponse> {
        val applicants = applicantRepository.findByLeadId(leadId)
        return applicants.map { mapEntityToResponse(it) }
    }

    @Cacheable(value = [CACHE_NAME], key = "'mobile_' + #mobileNumber")
    override fun getApplicantsByMobileNumber(mobileNumber: String): List<ApplicantResponse> {
        val personInfo = personClient.getPersonByMobile(mobileNumber).get()
        if (personInfo != null) {
            val applicants = applicantRepository.findByPersonId(personInfo.id)
            return applicants.map { mapEntityToResponse(it) }
        }
        return emptyList()
    }

    @Transactional
    @Caching(
        put = [CachePut(value = [CACHE_NAME], key = "#result.id")],
        evict = [CacheEvict(value = [CACHE_NAME], key = "'lead_' + #request.leadId")]
    )
    override fun createApplicant(request: ApplicantCreateRequest): ApplicantResponse {
        val applicant = Applicant(
            leadId = request.leadId,
            personId = request.personId,
            applicantType = request.applicantType,
            relationshipToPrimary = request.relationshipToPrimary,
            status = request.status
        )
        val savedApplicant = applicantRepository.save(applicant)
        return mapEntityToResponse(savedApplicant)
    }

    @Transactional
    @Caching(
        evict = [
            CacheEvict(value = [CACHE_NAME], key = "#applicantId"),
            CacheEvict(value = [CACHE_NAME], key = "'lead_' + #leadId")
        ]
    )
    override fun updateApplicant(applicantId: UUID, request: ApplicantUpdateRequest): ApplicantResponse {
        val applicant = applicantRepository.findById(applicantId)
            .orElseThrow { RuntimeException("Applicant not found with id: $applicantId") }

        // Update fields if provided
        request.applicantType?.let { applicant.applicantType = it }
        request.relationshipToPrimary?.let { applicant.relationshipToPrimary = it }
        request.status?.let { applicant.status = it }

        val updatedApplicant = applicantRepository.save(applicant)
        return mapEntityToResponse(updatedApplicant)
    }

    @Transactional
    @CacheEvict(value = [CACHE_NAME], key = "#applicantId")
    override fun deleteApplicant(applicantId: UUID) {
        val applicant = applicantRepository.findById(applicantId)
            .orElseThrow { RuntimeException("Applicant not found with id: $applicantId") }

        applicantRepository.deleteById(applicantId)
        cacheManager.getCache(CACHE_NAME)?.evict("lead_${applicant.leadId}")
    }

    private fun mapEntityToResponse(applicant: Applicant): ApplicantResponse {
        return ApplicantResponse(
            id = applicant.id,
            personId = applicant.personId,
            leadId = applicant.leadId,
            applicantType = applicant.applicantType,
            relationshipToPrimary = applicant.relationshipToPrimary,
            status = applicant.status
        )
    }
}
