package com.nivasafinance.features.advisorleadmapping.dto

import com.nivasafinance.features.advisorleadmapping.entity.AdvisorLeadMapping
import com.nivasafinance.features.payment.dto.PaymentCreateRequest
import java.util.UUID

data class AdvisorLeadMappingData(
    val id: UUID?,
    val advisorId: UUID,
    val leadId: UUID,
    val remarks: String?,
    val extData: Map<String, Any>?,
    val payment: PaymentCreateRequest?,
    val paymentId: UUID?
) {
    companion object {
        fun fromEntity(advisorLeadMapping: AdvisorLeadMapping): AdvisorLeadMappingData {
            return AdvisorLeadMappingData(
                id = advisorLeadMapping.id,
                advisorId = advisorLeadMapping.advisorId,
                leadId = advisorLeadMapping.leadId,
                remarks = advisorLeadMapping.remarks,
                extData = advisorLeadMapping.extData,
                payment = null,
                paymentId = advisorLeadMapping.paymentId
            )
        }

        fun fromEntityWithPayment(
            advisorLeadMapping: AdvisorLeadMapping,
            payment: PaymentCreateRequest?
        ): AdvisorLeadMappingData {
            return AdvisorLeadMappingData(
                id = advisorLeadMapping.id,
                advisorId = advisorLeadMapping.advisorId,
                leadId = advisorLeadMapping.leadId,
                remarks = advisorLeadMapping.remarks,
                extData = advisorLeadMapping.extData,
                payment = payment,
                paymentId = advisorLeadMapping.paymentId
            )
        }
    }
}
