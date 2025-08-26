package com.nivasafinance.features.wrapper.controller

import com.nivasafinance.features.wrapper.dto.ApplicantWrapperRequest
import com.nivasafinance.features.wrapper.dto.ApplicantWrapperResponse
import com.nivasafinance.features.wrapper.service.ApplicantWrapperService
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
@RequestMapping("/api/v1/applicant-wrapper")
class ApplicantWrapperController(
    private val applicantWrapperService: ApplicantWrapperService
) {

    @PostMapping
    fun createApplicant(
        @Valid @RequestBody request: ApplicantWrapperRequest
    ): ResponseEntity<ApplicantWrapperResponse> {
        val applicant = applicantWrapperService.createApplicant(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(applicant)
    }

    @GetMapping("/{applicantId}")
    fun getApplicant(@PathVariable applicantId: UUID): ResponseEntity<ApplicantWrapperResponse> {
        val applicant = applicantWrapperService.getApplicant(applicantId)
        return ResponseEntity.ok(applicant)
    }
}
