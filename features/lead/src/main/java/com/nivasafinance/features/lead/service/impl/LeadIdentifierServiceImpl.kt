package com.nivasafinance.features.lead.service.impl

import com.nivasafinance.features.lead.exception.LeadExceptionFactory
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper
import com.nivasafinance.features.lead.service.LeadIdentifierService
import com.nivasafinance.features.lead.dto.LeadIdentifierRequest
import com.nivasafinance.features.lead.dto.LeadIdentifierResponse
import com.nivasafinance.features.identifiers.dto.IdentifierUpdateRequest
import com.nivasafinance.features.identifiers.service.IdentifierService
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class LeadIdentifierServiceImpl(
    private val identifierService: IdentifierService,
    private val leadRepositoryWrapper: LeadRepositoryWrapper,
    private val messageSource: MessageSource
) : LeadIdentifierService {

    override fun getAllLeadIdentifiers(leadId: UUID): List<LeadIdentifierResponse> {
        val lead = leadRepositoryWrapper.findByIdWithException(leadId)
        return lead.identifierIds?.map { identifierId ->
            try {
                identifierService.getIdentifierById(identifierId)
            } catch (e: Exception) {
                throw LeadExceptionFactory.identifierRetrievalFailed(identifierId, leadId, messageSource)
            }
        } ?: emptyList()
    }

    override fun createLeadIdentifier(leadId: UUID, leadIdentifierRequest: LeadIdentifierRequest): LeadIdentifierResponse {
        val identifierResponse = try {
            identifierService.createIdentifier(leadIdentifierRequest.toIdentifierRequest())
        } catch (e: Exception) {
            throw LeadExceptionFactory.identifierCreationFailed(leadId, messageSource)
        }
        
        val lead = leadRepositoryWrapper.findByIdWithException(leadId)
        val currentIdentifierIds = lead.identifierIds ?: emptyList()
        lead.identifierIds = currentIdentifierIds + identifierResponse.id
        leadRepositoryWrapper.saveWithException(lead)
        
        return identifierResponse
    }

    override fun patchLeadIdentifier(leadId: UUID, identifierId: UUID, leadIdentifierUpdateRequest: IdentifierUpdateRequest): LeadIdentifierResponse {
        val identifierResponse = try {
            identifierService.updateIdentifier(identifierId, leadIdentifierUpdateRequest)
        } catch (e: Exception) {
            throw LeadExceptionFactory.identifierUpdateFailed(identifierId, leadId, messageSource)
        }
        
        return identifierResponse
    }
}