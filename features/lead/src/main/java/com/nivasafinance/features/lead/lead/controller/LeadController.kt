package com.nivasafinance.features.lead.lead.controller

import com.nivasafinance.features.lead.applicant.dto.ApplicantPatchRequest
import com.nivasafinance.features.lead.applicant.dto.ApplicantResponse
import com.nivasafinance.features.lead.applicant.repository.ApplicantRepository
import com.nivasafinance.features.lead.applicant.service.ApplicantReadService
import com.nivasafinance.features.lead.applicant.service.ApplicantWriteService
import com.nivasafinance.features.lead.lead.dto.LeadCreateRequest
import com.nivasafinance.features.lead.lead.dto.LeadPatchRequest
import com.nivasafinance.features.lead.lead.dto.LeadResponse
import com.nivasafinance.features.lead.lead.service.LeadReadService
import com.nivasafinance.features.lead.lead.service.LeadWriteService
import com.nivasafinance.features.person.repository.PersonRepository
import data.Identifier
import data.IdentifierResponse
import data.IdentifierType
import exception.ResourceNotFoundException
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("v1/lead")
class LeadController(
    private val applicantReadService: ApplicantReadService,
    private val leadWriteService: LeadWriteService,
    private val leadReadService: LeadReadService,
    private val applicantWriteService: ApplicantWriteService,
    private val applicantRepository: ApplicantRepository,
    private val personRepository: PersonRepository
) {
    @PostMapping
    fun createLead(@Valid @RequestBody request: LeadCreateRequest): ResponseEntity<LeadResponse> {
        val leadResponse = leadWriteService.createLead(request)
        val lead = leadReadService.getLeadById(leadResponse.id)
        return ResponseEntity.ok(lead)
    }

    @PatchMapping("/{id}")
    fun updateLead(
        @PathVariable id: UUID,
        @Valid @RequestBody request: LeadPatchRequest
    ): ResponseEntity<LeadResponse> {
        leadWriteService.patchLead(id, request)
        val updatedLead = leadReadService.getLeadById(id)
        return ResponseEntity.ok(updatedLead)
    }

    @PatchMapping("/{id}/applicant/{applicantId}")
    fun updateApplicant(
        @PathVariable applicantId: UUID,
        @Valid @RequestBody request: ApplicantPatchRequest
    ): ResponseEntity<ApplicantResponse> {
        applicantWriteService.patchApplicant(applicantId, request)
        val updatedApplicant = applicantReadService.getApplicantById(applicantId)
        return ResponseEntity.ok(updatedApplicant)
    }

    @PostMapping("/{id}/applicant/{applicantId}/identifier")
    fun addIdentifier(
        @PathVariable id: UUID,
        @PathVariable applicantId: UUID,
        @Valid @RequestBody request: Identifier
    ): ResponseEntity<IdentifierResponse> {
        leadWriteService.saveIdentifier(id, applicantId, request)
        val updatedApplicant = applicantRepository.findById(applicantId)
            .orElseThrow { ResourceNotFoundException("Applicant not found with id: $applicantId") }
        val person = personRepository.findById(updatedApplicant.personId)
            .orElseThrow { ResourceNotFoundException("Person not found for applicant: $applicantId") }
        val identifier = person.identifiers?.last()
            ?: throw ResourceNotFoundException("Identifier not found for applicant: $applicantId")
        return ResponseEntity.ok(
            IdentifierResponse(
                id = UUID.fromString(identifier.id),
                identifier = identifier.identifier,
                type = IdentifierType.valueOf(identifier.type.name)
            )
        )
    }

    @PutMapping("/{id}/applicant/{applicantId}/identifier/{identifierId}")
    fun updateIdentifier(
        @PathVariable id: UUID,
        @PathVariable applicantId: UUID,
        @PathVariable identifierId: UUID,
        @Valid @RequestBody request: Identifier
    ): ResponseEntity<IdentifierResponse> {
        leadWriteService.updateIdentifier(id, applicantId, identifierId, request)
        val updatedIdentifier = applicantReadService.getByIdentifierId(applicantId, identifierId)
        return ResponseEntity.ok(updatedIdentifier)
    }

    @GetMapping("/{id}")
    fun getLeadById(@PathVariable id: UUID): ResponseEntity<LeadResponse> {
        val lead = leadReadService.getLeadById(id)
        return ResponseEntity.ok(lead)
    }
}
