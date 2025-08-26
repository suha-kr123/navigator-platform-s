package com.nivasafinance.features.advisorleadmapping.service.impl

import base.BaseNavigatorService
import com.nivasafinance.features.advisorleadmapping.dto.AdvisorLeadMappingCreateRequest
import com.nivasafinance.features.advisorleadmapping.dto.AdvisorLeadMappingData
import com.nivasafinance.features.advisorleadmapping.dto.AdvisorLeadMappingResponse
import com.nivasafinance.features.advisorleadmapping.dto.AdvisorLeadMappingUpdateRequest
import com.nivasafinance.features.advisorleadmapping.exception.AdvisorLeadMappingByAdvisorAndLeadNotFoundException
import com.nivasafinance.features.advisorleadmapping.exception.AdvisorLeadMappingNoPaymentException
import com.nivasafinance.features.advisorleadmapping.service.AdvisorLeadMappingReadService
import com.nivasafinance.features.advisorleadmapping.service.AdvisorLeadMappingService
import com.nivasafinance.features.advisorleadmapping.service.AdvisorLeadMappingWriteService
import com.nivasafinance.features.payment.dto.PaymentCreateRequest
import com.nivasafinance.features.payment.dto.PaymentResponse
import com.nivasafinance.features.payment.dto.PaymentUpdateRequest
import com.nivasafinance.features.payment.service.PaymentService
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AdvisorLeadMappingServiceImpl(
    private val advisorLeadMappingReadService: AdvisorLeadMappingReadService,
    private val advisorLeadMappingWriteService: AdvisorLeadMappingWriteService,
    private val paymentService: PaymentService
) : AdvisorLeadMappingService, BaseNavigatorService() {

    companion object {
        private const val CACHE_NAME = "advisor_lead_mapping"
    }

    @Cacheable(cacheNames = [CACHE_NAME], key = "#id")
    override fun getAdvisorLeadMapping(id: UUID): AdvisorLeadMappingResponse {
        val mappingData = advisorLeadMappingReadService.getAdvisorLeadMappingData(id)
        return mapDataToResponse(mappingData)
    }

    @Cacheable(cacheNames = [CACHE_NAME], key = "#advisorId + '_' + #leadId")
    override fun getAdvisorLeadMappingByAdvisorAndLead(advisorId: UUID, leadId: UUID): AdvisorLeadMappingResponse? {
        val mappingData = advisorLeadMappingReadService.getAdvisorLeadMappingDataByAdvisorAndLead(advisorId, leadId)
        return mappingData?.let { mapDataToResponse(it) }
    }

    @Cacheable(cacheNames = [CACHE_NAME], key = "'advisor_' + #advisorId")
    override fun getAllLeadsForAdvisor(advisorId: UUID): List<AdvisorLeadMappingResponse> {
        val mappingDataList = advisorLeadMappingReadService.getAllLeadsForAdvisorData(advisorId)
        return mappingDataList.map { mappingData ->
            mapDataToResponse(mappingData)
        }
    }

    @Cacheable(cacheNames = [CACHE_NAME], key = "'mobile_' + #mobileNumber")
    override fun getAllLeadsForAdvisorByMobile(mobileNumber: String): List<AdvisorLeadMappingResponse> {
        val mappingDataList = advisorLeadMappingReadService.getAllLeadsForAdvisorByMobileData(mobileNumber)
        return mappingDataList.map { mappingData ->
            mapDataToResponse(mappingData)
        }
    }

    @Transactional
    @Caching(
        put = [CachePut(cacheNames = [CACHE_NAME], key = "#result.id")],
        evict = [
            CacheEvict(cacheNames = [CACHE_NAME], key = "#advisorId + '_' + #leadId"),
            CacheEvict(cacheNames = [CACHE_NAME], key = "'advisor_' + #advisorId"),
            CacheEvict(cacheNames = [CACHE_NAME], allEntries = true)
        ]
    )
    override fun createAdvisorLeadMapping(
        advisorId: UUID,
        leadId: UUID,
        request: AdvisorLeadMappingCreateRequest
    ): AdvisorLeadMappingResponse {
        val mappingData = advisorLeadMappingWriteService.createAdvisorLeadMappingData(advisorId, leadId, request)
        val response = mapDataToResponse(mappingData)
        return response
    }

    @Transactional
    @Caching(
        put = [CachePut(cacheNames = [CACHE_NAME], key = "#result.id")],
        evict = [
            CacheEvict(cacheNames = [CACHE_NAME], key = "#advisorId + '_' + #leadId"),
            CacheEvict(cacheNames = [CACHE_NAME], key = "'advisor_' + #advisorId"),
            CacheEvict(cacheNames = [CACHE_NAME], allEntries = true)
        ]
    )
    override fun updateAdvisorLeadMapping(
        advisorId: UUID,
        leadId: UUID,
        request: AdvisorLeadMappingUpdateRequest
    ): AdvisorLeadMappingResponse {
        val mapping = advisorLeadMappingReadService.getAdvisorLeadMappingDataByAdvisorAndLead(advisorId, leadId)
        if (mapping == null) {
            throw AdvisorLeadMappingByAdvisorAndLeadNotFoundException(advisorId, leadId, messageSource)
        }

        val mappingData = advisorLeadMappingWriteService.updateAdvisorLeadMappingData(mapping.id!!, request)
        return mapDataToResponse(mappingData)
    }

    @Transactional
    @Caching(
        evict = [
            CacheEvict(cacheNames = [CACHE_NAME], key = "#advisorId + '_' + #leadId"),
            CacheEvict(cacheNames = [CACHE_NAME], key = "'advisor_' + #advisorId"),
            CacheEvict(cacheNames = [CACHE_NAME], allEntries = true)
        ]
    )
    override fun deleteAdvisorLeadMapping(advisorId: UUID, leadId: UUID) {
        val mapping = advisorLeadMappingReadService.getAdvisorLeadMappingDataByAdvisorAndLead(advisorId, leadId)
        mapping?.let { advisorLeadMappingWriteService.deleteAdvisorLeadMapping(it.id!!) }
    }

    private fun mapDataToResponse(mappingData: AdvisorLeadMappingData): AdvisorLeadMappingResponse {
        val payment = mappingData.paymentId?.let { paymentService.getPayment(it) }
        return AdvisorLeadMappingResponse(
            id = mappingData.id!!,
            advisorId = mappingData.advisorId,
            leadId = mappingData.leadId,
            remarks = mappingData.remarks,
            extData = mappingData.extData,
            payment = payment
        )
    }

    @Transactional
    override fun createPaymentForLead(
        advisorId: UUID,
        leadId: UUID,
        request: PaymentCreateRequest
    ): PaymentResponse {
        val mapping = advisorLeadMappingReadService.getAdvisorLeadMappingDataByAdvisorAndLead(advisorId, leadId)
        if (mapping == null) {
            throw AdvisorLeadMappingByAdvisorAndLeadNotFoundException(advisorId, leadId, messageSource)
        }

        return paymentService.createPayment(request)
    }

    override fun getPaymentForLead(
        advisorId: UUID,
        leadId: UUID
    ): PaymentResponse {
        val mapping = advisorLeadMappingReadService.getAdvisorLeadMappingDataByAdvisorAndLead(advisorId, leadId)
        if (mapping == null) {
            throw AdvisorLeadMappingByAdvisorAndLeadNotFoundException(advisorId, leadId, messageSource)
        }

        if (mapping.paymentId == null) {
            throw AdvisorLeadMappingNoPaymentException(advisorId, messageSource)
        }

        return paymentService.getPayment(mapping.paymentId)
    }

    @Transactional
    override fun updatePaymentForLead(
        advisorId: UUID,
        leadId: UUID,
        request: PaymentUpdateRequest
    ): PaymentResponse {
        val mapping = advisorLeadMappingReadService.getAdvisorLeadMappingDataByAdvisorAndLead(advisorId, leadId)
        if (mapping == null) {
            throw AdvisorLeadMappingByAdvisorAndLeadNotFoundException(advisorId, leadId, messageSource)
        }

        if (mapping.paymentId == null) {
            throw AdvisorLeadMappingNoPaymentException(advisorId, messageSource)
        }

        return paymentService.updatePayment(mapping.paymentId, request)
    }

    @Transactional
    override fun deletePaymentForLead(
        advisorId: UUID,
        leadId: UUID
    ) {
        val mapping = advisorLeadMappingReadService.getAdvisorLeadMappingDataByAdvisorAndLead(advisorId, leadId)
        if (mapping == null) {
            throw AdvisorLeadMappingByAdvisorAndLeadNotFoundException(advisorId, leadId, messageSource)
        }

        if (mapping.paymentId == null) {
            throw AdvisorLeadMappingNoPaymentException(advisorId, messageSource)
        }

        paymentService.deletePayment(mapping.paymentId)
    }
}
