package com.nivasafinance.features.lead.service

import java.util.UUID
import com.nivasafinance.features.lead.dto.LeadIdentifierResponse
import com.nivasafinance.features.lead.dto.LeadIdentifierRequest
import com.nivasafinance.features.identifiers.dto.IdentifierUpdateRequest

interface LeadIdentifierService {
    fun getAllLeadIdentifiers(leadId: UUID): List<LeadIdentifierResponse>
    fun createLeadIdentifier(
            leadId: UUID,
            leadIdentifierRequest: LeadIdentifierRequest
    ): LeadIdentifierResponse
    fun patchLeadIdentifier(
            leadId: UUID,
            identifierId: UUID,
            leadIdentifierUpdateRequest: IdentifierUpdateRequest
    ): LeadIdentifierResponse
}

        