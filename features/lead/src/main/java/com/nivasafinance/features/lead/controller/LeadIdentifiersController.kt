package com.nivasafinance.features.lead.controller

import com.nivasafinance.features.lead.dto.LeadIdentifierRequest
import com.nivasafinance.features.lead.dto.LeadIdentifierResponse
import com.nivasafinance.features.identifiers.dto.IdentifierUpdateRequest
import com.nivasafinance.features.lead.service.LeadIdentifierService
import java.util.UUID
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/leads/{leadId}/identifiers")
class LeadIdentifiersController(private val leadIdentifierService: LeadIdentifierService) {

    @GetMapping
    fun getLeadIdentifiers(@PathVariable leadId: UUID): List<LeadIdentifierResponse> {
        return leadIdentifierService.getAllLeadIdentifiers(leadId)
    }

    @PostMapping
    fun createLeadIdentifier(
            @PathVariable leadId: UUID,
            @RequestBody leadIdentifierRequest: LeadIdentifierRequest
    ): LeadIdentifierResponse {
        return leadIdentifierService.createLeadIdentifier(leadId, leadIdentifierRequest)
    }

    @PatchMapping("/{identifierId}")
    fun patchLeadIdentifier(
            @PathVariable leadId: UUID,
            @PathVariable identifierId: UUID,
            @RequestBody leadIdentifierUpdateRequest: IdentifierUpdateRequest
    ): LeadIdentifierResponse {
        return leadIdentifierService.patchLeadIdentifier(
                leadId,
                identifierId,
                leadIdentifierUpdateRequest
        )
    }
}