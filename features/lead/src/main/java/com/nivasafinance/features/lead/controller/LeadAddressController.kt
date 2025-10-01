package com.nivasafinance.features.lead.controller

import com.nivasafinance.features.address.dto.AddressResponse
import com.nivasafinance.features.address.dto.CreateAddressRequest
import com.nivasafinance.features.address.dto.UpdateAddressRequest
import com.nivasafinance.features.lead.service.LeadAddressService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/leads")
class LeadAddressController(
    private val leadAddressService: LeadAddressService
) {

    @GetMapping("/{leadId}/address")
    fun getLeadAddress(
        @PathVariable leadId: UUID
    ): ResponseEntity<AddressResponse?> {
        val address = leadAddressService.getLeadAddress(leadId)
        return if (address != null) {
            ResponseEntity.ok(address)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("/{leadId}/address")
    fun createLeadAddress(
        @PathVariable leadId: UUID,
        @RequestBody addressRequest: CreateAddressRequest
    ): ResponseEntity<AddressResponse> {
        val address = leadAddressService.createLeadAddress(leadId, addressRequest)
        return ResponseEntity.status(HttpStatus.CREATED).body(address)
    }

    @PutMapping("/{leadId}/address")
    fun updateLeadAddress(
        @PathVariable leadId: UUID,
        @RequestBody addressRequest: UpdateAddressRequest
    ): ResponseEntity<AddressResponse> {
        val address = leadAddressService.updateLeadAddress(leadId, addressRequest)
        return ResponseEntity.ok(address)
    }
}
