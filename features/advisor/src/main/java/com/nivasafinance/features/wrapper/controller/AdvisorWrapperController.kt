package com.nivasafinance.features.wrapper.controller

import com.nivasafinance.features.wrapper.dto.AdvisorWrapperRequest
import com.nivasafinance.features.wrapper.dto.AdvisorWrapperResponse
import com.nivasafinance.features.wrapper.dto.CreateLeadForAdvisorRequest
import com.nivasafinance.features.wrapper.service.AdvisorWrapperService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/advisor-wrapper")
class AdvisorWrapperController(
    private val advisorWrapperService: AdvisorWrapperService
) {

    @PostMapping
    fun createAdvisor(@Valid @RequestBody request: AdvisorWrapperRequest): ResponseEntity<AdvisorWrapperResponse> {
        val advisor = advisorWrapperService.createAdvisor(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(advisor)
    }

    @GetMapping("/{advisorId}")
    fun getAdvisor(@PathVariable advisorId: UUID): ResponseEntity<AdvisorWrapperResponse> {
        val advisor = advisorWrapperService.getAdvisor(advisorId)
        return ResponseEntity.ok(advisor)
    }

    @PostMapping("/{advisorId}/leads")
    fun createLeadForAdvisor(
        @PathVariable advisorId: UUID,
        @Valid @RequestBody request: CreateLeadForAdvisorRequest
    ): ResponseEntity<AdvisorWrapperResponse> {
        val advisor = advisorWrapperService.createLeadForAdvisor(advisorId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(advisor)
    }
}
