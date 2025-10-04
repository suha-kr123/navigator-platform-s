package com.nivasafinance.features.lead.controller

import com.nivasafinance.common.base.model.PaginatedResponse
import com.nivasafinance.common.base.model.PaginationRequest
import com.nivasafinance.features.lead.dto.AddLeadPersonRequest
import com.nivasafinance.features.lead.dto.LeadCreateRequest
import com.nivasafinance.features.lead.dto.LeadPersonsResponse
import com.nivasafinance.features.lead.dto.LeadResponse
import com.nivasafinance.features.lead.dto.LeadUpdateRequest
import com.nivasafinance.features.lead.dto.UpdateLeadPersonRequest
import com.nivasafinance.features.lead.service.LeadService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/leads")
class LeadController(
    private val leadService: LeadService
) {

    @GetMapping("/persons/all")
    fun getAllLeadPersons(
        @RequestParam(defaultValue = "0") offset: Int,
        @RequestParam(defaultValue = "20") limit: Int,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "ASC") sortDirection: String,
        @RequestParam(required = false) search: String?
    ): ResponseEntity<List<LeadPersonsResponse>> {
        val paginationRequest = PaginationRequest(
            offset = offset,
            limit = limit,
            sortBy = sortBy,
            sortDirection = sortDirection
        )
        val leadPersons = leadService.getAllLeadPersons(paginationRequest, search)
        return ResponseEntity.ok(leadPersons)
    }

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
        @RequestParam(defaultValue = "ASC") sortDirection: String,
        @RequestParam(required = false) search: String?
    ): ResponseEntity<PaginatedResponse<LeadResponse>> {
        val paginationRequest = PaginationRequest(
            offset = offset,
            limit = limit,
            sortBy = sortBy,
            sortDirection = sortDirection
        )
        val leads = leadService.getAllLeads(paginationRequest, search)
        return ResponseEntity.ok(leads)
    }

    @PostMapping("/{leadId}/persons")
    fun addLeadPerson(
        @PathVariable leadId: UUID,
        @RequestBody addLeadPersonRequest: AddLeadPersonRequest
    ): ResponseEntity<LeadResponse> {
        val leadPerson = leadService.addLeadPerson(leadId, addLeadPersonRequest)
        return ResponseEntity.status(HttpStatus.CREATED).body(leadPerson)
    }

    @PatchMapping("/{leadId}/persons/{personId}")
    fun updateLeadPerson(
        @PathVariable leadId: UUID,
        @PathVariable personId: UUID,
        @RequestBody updateLeadPersonRequest: UpdateLeadPersonRequest
    ): ResponseEntity<LeadResponse> {
        val leadPerson = leadService.updateLeadPerson(leadId, personId, updateLeadPersonRequest)
        return ResponseEntity.ok(leadPerson)
    }
}
