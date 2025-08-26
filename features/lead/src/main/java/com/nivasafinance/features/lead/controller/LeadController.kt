package com.nivasafinance.features.lead.controller

import com.nivasafinance.features.lead.dto.LeadCreateRequest
import com.nivasafinance.features.lead.dto.LeadResponse
import com.nivasafinance.features.lead.dto.LeadUpdateRequest
import com.nivasafinance.features.lead.service.LeadService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("v1/lead")
class LeadController(
    private val leadService: LeadService
) {
    @PostMapping
    fun createLead(@Valid @RequestBody request: LeadCreateRequest): ResponseEntity<LeadResponse> {
        val leadResponse = leadService.createLead(request)
        return ResponseEntity.ok(leadResponse)
    }

    @PatchMapping("/{leadId}")
    fun updateLead(
        @PathVariable leadId: UUID,
        @Valid @RequestBody request: LeadUpdateRequest
    ): ResponseEntity<LeadResponse> {
        val updatedLead = leadService.updateLead(leadId, request)
        return ResponseEntity.ok(updatedLead)
    }

    @GetMapping("/{leadId}")
    fun getLeadById(@PathVariable leadId: UUID): ResponseEntity<LeadResponse> {
        val lead = leadService.getLeadById(leadId)
        return ResponseEntity.ok(lead)
    }
}
