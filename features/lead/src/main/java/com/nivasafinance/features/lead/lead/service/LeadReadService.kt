package com.nivasafinance.features.lead.lead.service

import com.nivasafinance.features.lead.lead.dto.LeadResponse
import java.util.UUID

interface LeadReadService {
    fun getLeadById(leadId: UUID): LeadResponse
}
