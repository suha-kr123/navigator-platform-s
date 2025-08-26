package com.nivasafinance.features.advisorleadmapping.service

import com.nivasafinance.features.advisorleadmapping.dto.AdvisorLeadMappingCreateRequest
import com.nivasafinance.features.advisorleadmapping.dto.AdvisorLeadMappingResponse
import com.nivasafinance.features.advisorleadmapping.dto.AdvisorLeadMappingUpdateRequest
import com.nivasafinance.features.payment.dto.PaymentCreateRequest
import com.nivasafinance.features.payment.dto.PaymentResponse
import com.nivasafinance.features.payment.dto.PaymentUpdateRequest
import java.util.UUID

interface AdvisorLeadMappingService {
    fun getAdvisorLeadMapping(id: UUID): AdvisorLeadMappingResponse
    fun getAdvisorLeadMappingByAdvisorAndLead(advisorId: UUID, leadId: UUID): AdvisorLeadMappingResponse?
    fun getAllLeadsForAdvisor(advisorId: UUID): List<AdvisorLeadMappingResponse>
    fun getAllLeadsForAdvisorByMobile(mobileNumber: String): List<AdvisorLeadMappingResponse>
    fun createAdvisorLeadMapping(
        advisorId: UUID,
        leadId: UUID,
        request: AdvisorLeadMappingCreateRequest
    ): AdvisorLeadMappingResponse
    fun updateAdvisorLeadMapping(
        advisorId: UUID,
        leadId: UUID,
        request: AdvisorLeadMappingUpdateRequest
    ): AdvisorLeadMappingResponse
    fun deleteAdvisorLeadMapping(advisorId: UUID, leadId: UUID)

    // Payment operations
    fun createPaymentForLead(
        advisorId: UUID,
        leadId: UUID,
        request: PaymentCreateRequest
    ): PaymentResponse

    fun getPaymentForLead(
        advisorId: UUID,
        leadId: UUID
    ): PaymentResponse

    fun updatePaymentForLead(
        advisorId: UUID,
        leadId: UUID,
        request: PaymentUpdateRequest
    ): PaymentResponse

    fun deletePaymentForLead(
        advisorId: UUID,
        leadId: UUID
    )
}
