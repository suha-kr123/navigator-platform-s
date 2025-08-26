package com.nivasafinance.features.advisor.controller

import com.nivasafinance.features.advisor.dto.AdvisorCreateRequest
import com.nivasafinance.features.advisor.dto.AdvisorResponse
import com.nivasafinance.features.advisor.dto.AdvisorUpdateRequest
import com.nivasafinance.features.advisor.service.AdvisorService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/advisors")
class AdvisorController(
    private val advisorService: AdvisorService
) {

    @PostMapping
    fun createAdvisor(@Valid @RequestBody request: AdvisorCreateRequest): ResponseEntity<AdvisorResponse> {
        val advisor = advisorService.createAdvisor(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(advisor)
    }

    @GetMapping("/{id}")
    fun getAdvisor(@PathVariable id: UUID): ResponseEntity<AdvisorResponse> {
        val advisor = advisorService.getAdvisor(id)
        return ResponseEntity.ok(advisor)
    }

    @PutMapping("/{id}")
    fun updateAdvisor(
        @PathVariable id: UUID,
        @Valid @RequestBody request: AdvisorUpdateRequest
    ): ResponseEntity<AdvisorResponse> {
        advisorService.updateAdvisor(id, request)
        val advisor = advisorService.getAdvisor(id)
        return ResponseEntity.ok(advisor)
    }

    @DeleteMapping("/{id}")
    fun deleteAdvisor(@PathVariable id: UUID): ResponseEntity<Unit> {
        advisorService.deleteAdvisor(id)
        return ResponseEntity.noContent().build()
    }
}
