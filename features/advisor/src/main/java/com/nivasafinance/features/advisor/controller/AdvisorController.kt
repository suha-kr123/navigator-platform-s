package com.nivasafinance.features.advisor.controller

import base.model.PaginatedResponse
import base.model.PaginationRequest
import base.model.SortDirection
import com.nivasafinance.features.advisor.dto.AdvisorRequest
import com.nivasafinance.features.advisor.dto.AdvisorResponse
import com.nivasafinance.features.advisor.dto.AdvisorUpdateRequest
import com.nivasafinance.features.advisor.service.AdvisorService
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
@RequestMapping("/api/advisors")
class AdvisorController(
    private val advisorService: AdvisorService
) {

    @PostMapping
    fun createAdvisor(@RequestBody advisorRequest: AdvisorRequest): ResponseEntity<AdvisorResponse> {
        val advisorResponse = advisorService.createAdvisor(advisorRequest)
        return ResponseEntity.status(HttpStatus.CREATED).body(advisorResponse)
    }

    @PutMapping("/{advisorId}")
    fun updateAdvisor(
        @PathVariable advisorId: UUID,
        @RequestBody updateAdvisorRequest: AdvisorUpdateRequest
    ): ResponseEntity<AdvisorResponse> {
        val advisorResponse = advisorService.updateAdvisor(advisorId, updateAdvisorRequest)
        return ResponseEntity.ok(advisorResponse)
    }

    @GetMapping("/{advisorId}")
    fun getAdvisor(@PathVariable advisorId: UUID): ResponseEntity<AdvisorResponse> {
        val advisorResponse = advisorService.getAdvisor(advisorId)
        return ResponseEntity.ok(advisorResponse)
    }

    @GetMapping
    fun getAllAdvisors(
        @RequestParam(defaultValue = "0") offset: Int,
        @RequestParam(defaultValue = "20") limit: Int,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "ASC") sortDirection: String
    ): ResponseEntity<PaginatedResponse<AdvisorResponse>> {
        val paginationRequest = PaginationRequest(
            offset = offset,
            limit = limit,
            sortBy = sortBy,
            sortDirection = SortDirection.valueOf(sortDirection.uppercase())
        )
        val advisorResponses = advisorService.getAllAdvisors(paginationRequest)
        return ResponseEntity.ok(advisorResponses)
    }

    @DeleteMapping("/{advisorId}")
    fun deleteAdvisor(@PathVariable advisorId: UUID): ResponseEntity<Unit> {
        advisorService.deleteAdvisor(advisorId)
        return ResponseEntity.noContent().build()
    }
}
