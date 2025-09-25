package com.nivasafinance.features.lead.controller

import base.model.PaginatedResponse
import base.model.PaginationRequest
import com.nivasafinance.features.lead.dto.LeadCreateRequest
import com.nivasafinance.features.lead.dto.LeadResponse
import com.nivasafinance.features.lead.dto.LeadUpdateRequest
import com.nivasafinance.features.lead.service.LeadService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/leads")
class LeadController(
    private val leadService: LeadService
) {

    @PostMapping
    fun createLead(@RequestBody leadCreateRequest: LeadCreateRequest): ResponseEntity<LeadResponse> {
        val lead = leadService.createLead(leadCreateRequest)
        return ResponseEntity.status(HttpStatus.CREATED).body(lead)
    }

    @GetMapping("/{id}")
    fun getLeadById(@PathVariable id: UUID): ResponseEntity<LeadResponse> {
        val lead = leadService.getLeadById(id)
        return ResponseEntity.ok(lead)
    }

    @PatchMapping("/{id}")
    fun updateLead(
        @PathVariable id: UUID,
        @RequestBody leadUpdateRequest: LeadUpdateRequest
    ): ResponseEntity<LeadResponse> {
        val updatedLead = leadService.updateLead(id, leadUpdateRequest)
        return ResponseEntity.ok(updatedLead)
    }

    @GetMapping("/all")
    fun getAllLeads(
        @RequestParam(defaultValue = "0") offset: Int,
        @RequestParam(defaultValue = "20") limit: Int,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "ASC") sortDirection: String
    ): ResponseEntity<PaginatedResponse<LeadResponse>> {
        val paginationRequest = PaginationRequest(
            offset = offset,
            limit = limit,
            sortBy = sortBy,
            sortDirection = sortDirection
        )
        val leads = leadService.getAllLeads(paginationRequest)
        return ResponseEntity.ok(leads)
    }

}
