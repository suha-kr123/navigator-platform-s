package com.nivasafinance.features.advisorleadmapping.controller

import base.model.PaginatedResponse
import base.model.PaginationRequest
import base.model.SortDirection
import com.nivasafinance.features.advisorleadmapping.dto.AdvisorLeadMappingRequest
import com.nivasafinance.features.advisorleadmapping.dto.AdvisorLeadMappingResponse
import com.nivasafinance.features.advisorleadmapping.dto.UpdateAdvisorLeadMappingRequest
import com.nivasafinance.features.advisorleadmapping.service.AdvisorLeadMappingService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/advisor-lead-mappings")
class AdvisorLeadMappingController(
    private val advisorLeadMappingService: AdvisorLeadMappingService
) {

    @PostMapping
    fun createAdvisorLeadMapping(
        @RequestBody advisorLeadMappingRequest: AdvisorLeadMappingRequest
    ): ResponseEntity<AdvisorLeadMappingResponse> {
        val mappingResponse = advisorLeadMappingService.createAdvisorLeadMapping(advisorLeadMappingRequest)
        return ResponseEntity.status(HttpStatus.CREATED).body(mappingResponse)
    }

    @PutMapping("/{mappingId}")
    fun updateAdvisorLeadMapping(
        @PathVariable mappingId: UUID,
        @RequestBody updateRequest: UpdateAdvisorLeadMappingRequest
    ): ResponseEntity<AdvisorLeadMappingResponse> {
        val mappingResponse = advisorLeadMappingService.updateAdvisorLeadMapping(mappingId, updateRequest)
        return ResponseEntity.ok(mappingResponse)
    }

    @GetMapping("/{mappingId}")
    fun getAdvisorLeadMapping(@PathVariable mappingId: UUID): ResponseEntity<AdvisorLeadMappingResponse> {
        val mappingResponse = advisorLeadMappingService.getAdvisorLeadMapping(mappingId)
        return ResponseEntity.ok(mappingResponse)
    }

    @GetMapping("/advisor/{advisorId}")
    fun getAllLeadsOfAdvisor(
        @PathVariable advisorId: UUID,
        @RequestParam(defaultValue = "0") offset: Int,
        @RequestParam(defaultValue = "20") limit: Int,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "ASC") sortDirection: String
    ): ResponseEntity<PaginatedResponse<AdvisorLeadMappingResponse>> {
        val paginationRequest = PaginationRequest(
            offset = offset,
            limit = limit,
            sortBy = sortBy,
            sortDirection = SortDirection.valueOf(sortDirection.uppercase())
        )
        val mappingResponses = advisorLeadMappingService.getAllLeadsOfAdvisor(advisorId, paginationRequest)
        return ResponseEntity.ok(mappingResponses)
    }

    @DeleteMapping("/{mappingId}")
    fun deleteAdvisorLeadMapping(@PathVariable mappingId: UUID): ResponseEntity<Unit> {
        advisorLeadMappingService.deleteAdvisorLeadMapping(mappingId)
        return ResponseEntity.noContent().build()
    }
}
