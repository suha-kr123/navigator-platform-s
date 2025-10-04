package com.nivasafinance.features.leadlender.controller

import com.nivasafinance.features.leadlender.dto.CreateLeadLenderRequest
import com.nivasafinance.features.leadlender.dto.CreateLeadLenderResponse
import com.nivasafinance.features.leadlender.dto.LeadLenderResponse
import com.nivasafinance.features.leadlender.dto.RejectLeadLenderRequest
import com.nivasafinance.features.leadlender.dto.UpdateLeadLenderRequest
import com.nivasafinance.features.leadlender.service.LeadLenderReadService
import com.nivasafinance.features.leadlender.service.LeadLenderWriteService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/lead/{leadId}/lender")
class LeadLenderController(
    private val leadLenderReadService: LeadLenderReadService,
    private val leadLenderWriteService: LeadLenderWriteService
) {

    @PostMapping
    fun createLeadLender(
        @PathVariable leadId: UUID,
        @Valid @RequestBody request: CreateLeadLenderRequest
    ): CreateLeadLenderResponse {
        return leadLenderWriteService.createLeadLender(leadId, request)
    }

    @PatchMapping("/{leadLenderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun updateLeadLender(
        @PathVariable leadId: UUID,
        @PathVariable leadLenderId: UUID,
        @Valid @RequestBody request: UpdateLeadLenderRequest
    ) {
        leadLenderWriteService.updateLeadLender(leadLenderId, request)
    }

    @PostMapping("/{leadLenderId}/reject")
    @ResponseStatus(HttpStatus.OK)
    fun rejectLeadLender(
        @PathVariable leadId: UUID,
        @PathVariable leadLenderId: UUID,
        @Valid @RequestBody request: RejectLeadLenderRequest
    ) {
        leadLenderWriteService.rejectLeadLender(leadLenderId, request)
    }

    @GetMapping("/all")
    fun getLeadLenders(
        @PathVariable leadId: UUID
    ): List<LeadLenderResponse> {
        return leadLenderReadService.getLeadLenders(leadId)
    }
}
