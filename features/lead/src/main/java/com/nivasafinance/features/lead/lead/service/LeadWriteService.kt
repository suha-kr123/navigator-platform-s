package com.nivasafinance.features.lead.lead.service

import com.nivasafinance.features.lead.lead.dto.LeadCreateRequest
import com.nivasafinance.features.lead.lead.dto.LeadPatchRequest
import com.nivasafinance.features.lead.lead.dto.LeadResponse
import data.Identifier
import java.util.UUID

interface LeadWriteService {
    fun createLead(request: LeadCreateRequest): LeadResponse
    fun patchLead(id: UUID, request: LeadPatchRequest)
    fun saveIdentifier(id: UUID, applicantId: UUID, addIdentifier: Identifier)
    fun updateIdentifier(id: UUID, applicantId: UUID, identifierId: UUID, addIdentifier: Identifier)
}
