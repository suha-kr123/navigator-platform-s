package com.nivasafinance.features.advisorleadmapping.service.impl

import base.BaseNavigatorService
import com.nivasafinance.features.advisorleadmapping.client.AdvisorClient
import com.nivasafinance.features.advisorleadmapping.client.LeadClient
import com.nivasafinance.features.advisorleadmapping.client.PaymentClient
import com.nivasafinance.features.advisorleadmapping.client.PaymentCreateRequest
import com.nivasafinance.features.advisorleadmapping.client.PaymentResponse
import com.nivasafinance.features.advisorleadmapping.client.PaymentUpdateRequest
import com.nivasafinance.features.advisorleadmapping.dto.AdvisorLeadMappingCreateRequest
import com.nivasafinance.features.advisorleadmapping.dto.AdvisorLeadMappingResponse
import com.nivasafinance.features.advisorleadmapping.dto.AdvisorLeadMappingUpdateRequest
import com.nivasafinance.features.advisorleadmapping.entity.AdvisorLeadMapping
import com.nivasafinance.features.advisorleadmapping.exception.AdvisorLeadMappingAlreadyExistsException
import com.nivasafinance.features.advisorleadmapping.exception.AdvisorLeadMappingByAdvisorAndLeadNotFoundException
import com.nivasafinance.features.advisorleadmapping.exception.AdvisorLeadMappingNoPaymentException
import com.nivasafinance.features.advisorleadmapping.exception.AdvisorLeadMappingNotFoundException
import com.nivasafinance.features.advisorleadmapping.repository.AdvisorLeadMappingRepository
import com.nivasafinance.features.advisorleadmapping.service.AdvisorLeadMappingService
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@CacheConfig(cacheManager = "advisorLeadMappingCacheManager")
@Suppress("ImportOrdering", "NoTrailingSpaces", "UnreachableCode", "UnsafeCallOnNullableType")
class AdvisorLeadMappingServiceImpl(
    private val advisorLeadMappingRepository: AdvisorLeadMappingRepository,
    private val advisorClient: AdvisorClient,
    private val leadClient: LeadClient,
    private val paymentClient: PaymentClient
) : AdvisorLeadMappingService, BaseNavigatorService() {

    companion object {
        private const val CACHE_NAME = "advisor_lead_mapping"
    }

    @Cacheable(cacheNames = [CACHE_NAME], key = "#id")
    override fun getAdvisorLeadMapping(id: UUID): AdvisorLeadMappingResponse {
        val mapping = advisorLeadMappingRepository.findById(id)
            .orElseThrow { AdvisorLeadMappingNotFoundException(id, messageSource) }
        return mapEntityToResponse(mapping)
    }

    @Cacheable(cacheNames = [CACHE_NAME], key = "#advisorId + '_' + #leadId")
    override fun getAdvisorLeadMappingByAdvisorAndLead(advisorId: UUID, leadId: UUID): AdvisorLeadMappingResponse? {
        val mapping = advisorLeadMappingRepository.findByAdvisorIdAndLeadId(advisorId, leadId)
        return mapping?.let { mapEntityToResponse(it) }
    }

    @Cacheable(cacheNames = [CACHE_NAME], key = "'advisor_' + #advisorId")
    override fun getAllLeadsForAdvisor(advisorId: UUID): List<AdvisorLeadMappingResponse> {
        val mappings = advisorLeadMappingRepository.findByAdvisorId(advisorId)
        return mappings.map { mapping ->
            mapEntityToResponse(mapping)
        }
    }

    @Cacheable(cacheNames = [CACHE_NAME], key = "'mobile_' + #mobileNumber")
    override fun getAllLeadsForAdvisorByMobile(mobileNumber: String): List<AdvisorLeadMappingResponse> {
        val advisor = advisorClient.getAdvisorByMobile(mobileNumber)
        if (advisor == null) return emptyList()

        val mappings = advisorLeadMappingRepository.findByAdvisorId(advisor.id)
        return mappings.map { mapping ->
            mapEntityToResponse(mapping)
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
        // Validate advisor and lead exist
        advisorClient.validateAdvisor(advisorId)
        leadClient.validateLead(leadId)

        // Check if mapping already exists
        if (advisorLeadMappingRepository.existsByAdvisorIdAndLeadId(advisorId, leadId)) {
            throw AdvisorLeadMappingAlreadyExistsException(advisorId, leadId, messageSource)
        }

        // Create the mapping
        val mapping = AdvisorLeadMapping(
            advisorId = advisorId,
            leadId = leadId,
            remarks = request.remarks,
            extData = request.extData
        )

        val savedMapping = advisorLeadMappingRepository.save(mapping)
        return mapEntityToResponse(savedMapping)
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
        val existingMapping = advisorLeadMappingRepository.findByAdvisorIdAndLeadId(advisorId, leadId)
            ?: throw AdvisorLeadMappingByAdvisorAndLeadNotFoundException(advisorId, leadId, messageSource)

        // Handle payment creation if provided
        request.payment?.let { paymentRequest ->
            val createRequest = PaymentCreateRequest(
                advisorId = existingMapping.advisorId,
                leadId = existingMapping.leadId,
                amount = paymentRequest.amount ?: error("Amount is required"),
                paymentType = paymentRequest.paymentType ?: error("Payment type is required"),
                description = paymentRequest.description
            )
            paymentClient.createPayment(createRequest)
        }

        val updatedMapping = AdvisorLeadMapping(
            id = existingMapping.id!!,
            advisorId = existingMapping.advisorId,
            leadId = existingMapping.leadId,
            remarks = request.remarks ?: existingMapping.remarks,
            extData = request.extData ?: existingMapping.extData
        )

        val savedMapping = advisorLeadMappingRepository.save(updatedMapping)
        return mapEntityToResponse(savedMapping)
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
        val mapping = advisorLeadMappingRepository.findByAdvisorIdAndLeadId(advisorId, leadId)
        mapping?.let {
            // Note: We could check if there are any payments associated with this mapping
            // by querying the payment service with entityId and entityType
            // For now, we'll allow deletion without this check
            advisorLeadMappingRepository.deleteById(it.id!!)
        }
    }

    private fun mapEntityToResponse(mapping: AdvisorLeadMapping): AdvisorLeadMappingResponse {
        return AdvisorLeadMappingResponse(
            id = mapping.id!!,
            advisorId = mapping.advisorId,
            leadId = mapping.leadId,
            remarks = mapping.remarks,
            extData = mapping.extData,
            payment = null
        )
    }

    @Transactional
    override fun createPaymentForLead(
        advisorId: UUID,
        leadId: UUID,
        request: PaymentCreateRequest
    ): PaymentResponse {
        val mapping = advisorLeadMappingRepository.findByAdvisorIdAndLeadId(advisorId, leadId)
            ?: throw AdvisorLeadMappingByAdvisorAndLeadNotFoundException(advisorId, leadId, messageSource)

        val paymentRequest = PaymentCreateRequest(
            advisorId = mapping.advisorId,
            leadId = mapping.leadId,
            amount = request.amount ?: error("Amount is required"),
            paymentType = request.paymentType ?: error("Payment type is required"),
            description = request.description
        )
        return paymentClient.createPayment(paymentRequest)
    }

    override fun getPaymentForLead(
        advisorId: UUID,
        leadId: UUID
    ): PaymentResponse {
        val mapping = advisorLeadMappingRepository.findByAdvisorIdAndLeadId(advisorId, leadId)
            ?: throw AdvisorLeadMappingByAdvisorAndLeadNotFoundException(advisorId, leadId, messageSource)

        if (mapping.id == null) {
            throw AdvisorLeadMappingNoPaymentException(advisorId, messageSource)
        }

        // Query payment by entityId and entityType
        // Note: This would need to be implemented in PaymentService/Repository
        // For now, we'll throw an exception indicating this needs to be implemented
        throw AdvisorLeadMappingNoPaymentException(advisorId, messageSource)
    }

    @Transactional
    override fun updatePaymentForLead(
        advisorId: UUID,
        leadId: UUID,
        request: PaymentUpdateRequest
    ): PaymentResponse {
        val mapping = advisorLeadMappingRepository.findByAdvisorIdAndLeadId(advisorId, leadId)
            ?: throw AdvisorLeadMappingByAdvisorAndLeadNotFoundException(advisorId, leadId, messageSource)

        if (mapping.id == null) {
            throw AdvisorLeadMappingNoPaymentException(advisorId, messageSource)
        }

        // Note: This would need to be implemented to find payment by entityId and entityType
        // For now, we'll throw an exception indicating this needs to be implemented
        throw AdvisorLeadMappingNoPaymentException(advisorId, messageSource)
    }

    @Transactional
    override fun deletePaymentForLead(
        advisorId: UUID,
        leadId: UUID
    ) {
        val mapping = advisorLeadMappingRepository.findByAdvisorIdAndLeadId(advisorId, leadId)
            ?: throw AdvisorLeadMappingByAdvisorAndLeadNotFoundException(advisorId, leadId, messageSource)

        if (mapping.id == null) {
            throw AdvisorLeadMappingNoPaymentException(advisorId, messageSource)
        }

        // Note: This would need to be implemented to find and delete payment by entityId and entityType
        // For now, we'll throw an exception indicating this needs to be implemented
        throw AdvisorLeadMappingNoPaymentException(advisorId, messageSource)
    }
}
