package com.nivasafinance.features.leadlender.service

import com.nivasafinance.features.leadlender.dto.LeadLenderResponse
import java.util.UUID

interface LeadLenderReadService {
    fun getLeadLenders(leadId: UUID): List<LeadLenderResponse>
    fun getLeadLenderById(leadLenderId: UUID): LeadLenderResponse
}
