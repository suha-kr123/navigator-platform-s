package com.nivasafinance.features.leadlender.service

import com.nivasafinance.features.leadlender.dto.CreateLeadLenderRequest
import com.nivasafinance.features.leadlender.dto.CreateLeadLenderResponse
import com.nivasafinance.features.leadlender.dto.RejectLeadLenderRequest
import com.nivasafinance.features.leadlender.dto.UpdateLeadLenderRequest
import java.util.UUID

interface LeadLenderWriteService {
    fun createLeadLender(leadId: UUID, request: CreateLeadLenderRequest): CreateLeadLenderResponse
    fun updateLeadLender(lenderId: UUID, request: UpdateLeadLenderRequest)
    fun rejectLeadLender(lenderId: UUID, request: RejectLeadLenderRequest)
}
