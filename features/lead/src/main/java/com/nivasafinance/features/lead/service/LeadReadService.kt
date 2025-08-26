package com.nivasafinance.features.lead.service

import com.nivasafinance.features.lead.dto.LeadResponse
import java.util.UUID

interface LeadReadService {
    fun getLeadById(leadId: UUID): LeadResponse
}
