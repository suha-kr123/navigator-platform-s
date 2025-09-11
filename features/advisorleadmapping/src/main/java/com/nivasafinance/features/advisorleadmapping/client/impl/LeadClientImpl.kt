package com.nivasafinance.features.advisorleadmapping.client.impl

import com.nivasafinance.features.advisorleadmapping.client.LeadClient
import com.nivasafinance.features.advisorleadmapping.client.LeadInfo
import event.EventTopics
import event.GetLeadRequest
import event.ValidateLeadRequest
import event.impl.SpringEventService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.UUID

@Component
@Suppress("NoWildcardImports", "NoUnusedImports", "NoTrailingSpaces", "WildcardImport", "VarCouldBeVal")
class LeadClientImpl : LeadClient {

    @Autowired
    private lateinit var eventService: SpringEventService

    override fun getLead(id: UUID): LeadInfo {
        val requestId = UUID.randomUUID().toString()
        val correlationId = UUID.randomUUID().toString()

        val request = GetLeadRequest(
            requestId = requestId,
            leadId = id,
            correlationId = correlationId
        )

        eventService.publishEvent(request, EventTopics.LEAD_GET_REQUEST)

        // In a real implementation, you'd wait for the response event
        // For now, we'll simulate a successful response
        return LeadInfo(
            id = id,
            requestedAmount = java.math.BigDecimal("100000"),
            purpose = "Home Loan",
            productCode = "HL001",
            status = "ACTIVE",
            stage = "APPROVED"
        )
    }

    override fun validateLead(id: UUID): Boolean {
        val requestId = UUID.randomUUID().toString()
        val correlationId = UUID.randomUUID().toString()

        val request = ValidateLeadRequest(
            requestId = requestId,
            leadId = id,
            correlationId = correlationId
        )

        eventService.publishEvent(request, EventTopics.LEAD_VALIDATE_REQUEST)

        // In a real implementation, you'd wait for the response event
        return true
    }
}
