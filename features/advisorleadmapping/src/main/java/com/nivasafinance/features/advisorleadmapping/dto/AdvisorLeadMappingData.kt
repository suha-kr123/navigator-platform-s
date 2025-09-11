package com.nivasafinance.features.advisorleadmapping.dto

import com.nivasafinance.features.advisorleadmapping.entity.AdvisorLeadMapping
import java.util.UUID

data class AdvisorLeadMappingData(
    val id: UUID?,
    val advisorId: UUID,
    val leadId: UUID,
    val remarks: String?,
    val extData: Map<String, Any>?
) {
    companion object {
        fun fromEntity(advisorLeadMapping: AdvisorLeadMapping): AdvisorLeadMappingData {
            return AdvisorLeadMappingData(
                id = advisorLeadMapping.id,
                advisorId = advisorLeadMapping.advisorId,
                leadId = advisorLeadMapping.leadId,
                remarks = advisorLeadMapping.remarks,
                extData = advisorLeadMapping.extData
            )
        }
    }
}
