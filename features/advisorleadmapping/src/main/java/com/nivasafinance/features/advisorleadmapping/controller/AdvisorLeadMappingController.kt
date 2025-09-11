package com.nivasafinance.features.advisorleadmapping.controller

import com.nivasafinance.features.advisorleadmapping.client.PaymentCreateRequest
import com.nivasafinance.features.advisorleadmapping.client.PaymentResponse
import com.nivasafinance.features.advisorleadmapping.client.PaymentUpdateRequest
import com.nivasafinance.features.advisorleadmapping.dto.AdvisorLeadMappingCreateRequest
import com.nivasafinance.features.advisorleadmapping.dto.AdvisorLeadMappingResponse
import com.nivasafinance.features.advisorleadmapping.dto.AdvisorLeadMappingUpdateRequest
import com.nivasafinance.features.advisorleadmapping.service.AdvisorLeadMappingService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
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
@RequestMapping("/v1/advisors")
@Suppress("ImportOrdering")
class AdvisorLeadMappingController(
    private val advisorLeadMappingService: AdvisorLeadMappingService
) {

    @PostMapping("/{advisorId}/leads/{leadId}")
    fun createAdvisorLeadMapping(
        @PathVariable advisorId: UUID,
        @PathVariable leadId: UUID,
        @RequestBody @Valid request: AdvisorLeadMappingCreateRequest
    ): ResponseEntity<AdvisorLeadMappingResponse> {
        val mapping = advisorLeadMappingService.createAdvisorLeadMapping(advisorId, leadId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(mapping)
    }

    @GetMapping("/{advisorId}/leads")
    fun getAllLeadsForAdvisor(
        @PathVariable advisorId: UUID
    ): ResponseEntity<List<AdvisorLeadMappingResponse>> {
        val mappings = advisorLeadMappingService.getAllLeadsForAdvisor(advisorId)
        return ResponseEntity.ok(mappings)
    }

    @GetMapping("mobile/{mobileNumber}/leads")
    fun getAllLeadsForAdvisorByMobile(
        @PathVariable mobileNumber: String
    ): ResponseEntity<List<AdvisorLeadMappingResponse>> {
        val mappings = advisorLeadMappingService.getAllLeadsForAdvisorByMobile(mobileNumber)
        return ResponseEntity.ok(mappings)
    }

    @PatchMapping("/{advisorId}/leads/{leadId}")
    fun updateAdvisorLeadMapping(
        @PathVariable advisorId: UUID,
        @PathVariable leadId: UUID,
        @RequestBody @Valid request: AdvisorLeadMappingUpdateRequest
    ): ResponseEntity<AdvisorLeadMappingResponse> {
        val mapping = advisorLeadMappingService.updateAdvisorLeadMapping(advisorId, leadId, request)
        return ResponseEntity.ok(mapping)
    }

    @DeleteMapping("/{advisorId}/leads/{leadId}")
    fun deleteAdvisorLeadMapping(
        @PathVariable advisorId: UUID,
        @PathVariable leadId: UUID
    ): ResponseEntity<Unit> {
        advisorLeadMappingService.deleteAdvisorLeadMapping(advisorId, leadId)
        return ResponseEntity.noContent().build()
    }

    // ========== PAYMENT APIs ==========

    @PostMapping("/{advisorId}/leads/{leadId}/payments")
    fun createPaymentForLead(
        @PathVariable advisorId: UUID,
        @PathVariable leadId: UUID,
        @RequestBody @Valid request: PaymentCreateRequest
    ): ResponseEntity<PaymentResponse> {
        val payment = advisorLeadMappingService.createPaymentForLead(advisorId, leadId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(payment)
    }

    @GetMapping("/{advisorId}/leads/{leadId}/payment")
    fun getPaymentForLead(
        @PathVariable advisorId: UUID,
        @PathVariable leadId: UUID
    ): ResponseEntity<PaymentResponse> {
        val payment = advisorLeadMappingService.getPaymentForLead(advisorId, leadId)
        return ResponseEntity.ok(payment)
    }

    @PutMapping("/{advisorId}/leads/{leadId}/payment")
    fun updatePaymentForLead(
        @PathVariable advisorId: UUID,
        @PathVariable leadId: UUID,
        @RequestBody @Valid request: PaymentUpdateRequest
    ): ResponseEntity<PaymentResponse> {
        val payment = advisorLeadMappingService.updatePaymentForLead(advisorId, leadId, request)
        return ResponseEntity.ok(payment)
    }

    @DeleteMapping("/{advisorId}/leads/{leadId}/payment")
    fun deletePaymentForLead(
        @PathVariable advisorId: UUID,
        @PathVariable leadId: UUID
    ): ResponseEntity<Unit> {
        advisorLeadMappingService.deletePaymentForLead(advisorId, leadId)
        return ResponseEntity.noContent().build()
    }
}
