package com.nivasafinance.features.advisorleadmapping.service.impl

import base.BaseNavigatorService
import com.nivasafinance.features.advisor.service.AdvisorService
import com.nivasafinance.features.advisorleadmapping.dto.AdvisorLeadMappingCreateRequest
import com.nivasafinance.features.advisorleadmapping.dto.AdvisorLeadMappingData
import com.nivasafinance.features.advisorleadmapping.dto.AdvisorLeadMappingUpdateRequest
import com.nivasafinance.features.advisorleadmapping.entity.AdvisorLeadMapping
import com.nivasafinance.features.advisorleadmapping.exception.AdvisorLeadMappingAlreadyExistsException
import com.nivasafinance.features.advisorleadmapping.exception.AdvisorLeadMappingNotFoundException
import com.nivasafinance.features.advisorleadmapping.exception.AdvisorLeadMappingWithPaymentException
import com.nivasafinance.features.advisorleadmapping.repository.AdvisorLeadMappingRepository
import com.nivasafinance.features.advisorleadmapping.service.AdvisorLeadMappingWriteService
import com.nivasafinance.features.payment.dto.PaymentCreateRequest
import com.nivasafinance.features.payment.service.PaymentService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AdvisorLeadMappingWriteServiceImpl(
    private val advisorLeadMappingRepository: AdvisorLeadMappingRepository,
    private val advisorService: AdvisorService,
    private val paymentService: PaymentService
) : AdvisorLeadMappingWriteService, BaseNavigatorService() {

    @Transactional
    override fun createAdvisorLeadMappingData(
        advisorId: UUID,
        leadId: UUID,
        request: AdvisorLeadMappingCreateRequest
    ): AdvisorLeadMappingData {
        advisorService.getAdvisor(advisorId)

        if (advisorLeadMappingRepository.existsByAdvisorIdAndLeadId(advisorId, leadId)) {
            throw AdvisorLeadMappingAlreadyExistsException(advisorId, leadId, messageSource)
        }

        val paymentId = request.payment?.let { paymentRequest ->
            val paymentResponse = paymentService.createPayment(paymentRequest)
            paymentResponse.id
        }

        val mapping = AdvisorLeadMapping(
            advisorId = advisorId,
            leadId = leadId,
            paymentId = paymentId,
            remarks = request.remarks,
            extData = request.extData
        )

        val savedMapping = advisorLeadMappingRepository.save(mapping)
        return AdvisorLeadMappingData.fromEntity(savedMapping)
    }

    @Transactional
    override fun updateAdvisorLeadMappingData(
        id: UUID,
        request: AdvisorLeadMappingUpdateRequest
    ): AdvisorLeadMappingData {
        val existingMapping = advisorLeadMappingRepository.findById(id)
            .orElseThrow { AdvisorLeadMappingNotFoundException(id, messageSource) }

        val paymentId = when {
            request.payment != null && existingMapping.paymentId != null -> {
                paymentService.updatePayment(existingMapping.paymentId, request.payment)
                existingMapping.paymentId
            }
            request.payment != null && existingMapping.paymentId == null -> {
                val createRequest = PaymentCreateRequest(
                    paymentStatus = request.payment.paymentStatus,
                    amountPaid = request.payment.amountPaid,
                    paidAt = request.payment.paidAt,
                    paymentMethod = request.payment.paymentMethod,
                    transactionId = request.payment.transactionId,
                    remarks = request.payment.remarks,
                    extData = request.payment.extData
                )
                val paymentResponse = paymentService.createPayment(createRequest)
                paymentResponse.id
            }
            else -> existingMapping.paymentId
        }

        val updatedMapping = AdvisorLeadMapping(
            id = existingMapping.id!!,
            advisorId = existingMapping.advisorId,
            leadId = existingMapping.leadId,
            paymentId = paymentId,
            remarks = request.remarks ?: existingMapping.remarks,
            extData = request.extData ?: existingMapping.extData
        )

        val savedMapping = advisorLeadMappingRepository.save(updatedMapping)
        return AdvisorLeadMappingData.fromEntity(savedMapping)
    }

    @Transactional
    override fun deleteAdvisorLeadMapping(id: UUID) {
        val existingMapping = advisorLeadMappingRepository.findById(id)
            .orElseThrow { AdvisorLeadMappingNotFoundException(id, messageSource) }

        if (existingMapping.paymentId != null) {
            throw AdvisorLeadMappingWithPaymentException(
                existingMapping.advisorId,
                existingMapping.leadId,
                messageSource
            )
        }

        advisorLeadMappingRepository.deleteById(existingMapping.id!!)
    }
}
