package com.nivasafinance.features.applicant.controller

import com.nivasafinance.features.applicant.dto.ApplicantCreateRequest
import com.nivasafinance.features.applicant.dto.ApplicantResponse
import com.nivasafinance.features.applicant.dto.ApplicantUpdateRequest
import com.nivasafinance.features.applicant.service.ApplicantService
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
@RequestMapping("/v1/applicants")
class ApplicantController(
    private val applicantService: ApplicantService
) {

    @PostMapping
    fun createApplicant(@RequestBody @Valid request: ApplicantCreateRequest): ResponseEntity<ApplicantResponse> {
        val applicant = applicantService.createApplicant(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(applicant)
    }

    @GetMapping("/{applicantId}")
    fun getApplicant(@PathVariable applicantId: UUID): ResponseEntity<ApplicantResponse> {
        val applicant = applicantService.getApplicant(applicantId)
        return ResponseEntity.ok(applicant)
    }

    @PutMapping("/{applicantId}")
    fun updateApplicant(
        @PathVariable applicantId: UUID,
        @RequestBody @Valid request: ApplicantUpdateRequest
    ): ResponseEntity<ApplicantResponse> {
        val applicant = applicantService.updateApplicant(applicantId, request)
        return ResponseEntity.ok(applicant)
    }

    @DeleteMapping("/{applicantId}")
    fun deleteApplicant(@PathVariable applicantId: UUID): ResponseEntity<Unit> {
        applicantService.deleteApplicant(applicantId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/lead/{leadId}")
    fun getApplicantsByLead(@PathVariable leadId: UUID): ResponseEntity<List<ApplicantResponse>> {
        val applicants = applicantService.getApplicantsByLeadId(leadId)
        return ResponseEntity.ok(applicants)
    }
}
