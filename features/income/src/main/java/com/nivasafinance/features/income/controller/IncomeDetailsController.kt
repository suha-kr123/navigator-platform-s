package com.nivasafinance.features.income.controller

import com.nivasafinance.features.income.dto.IncomeDetailsRequest
import com.nivasafinance.features.income.dto.IncomeDetailsResponse
import com.nivasafinance.features.income.dto.IncomeDetailsUpdateRequest
import com.nivasafinance.features.income.service.IncomeDetailsService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/income-details")
class IncomeDetailsController(private val incomeDetailsService: IncomeDetailsService) {

    @GetMapping
    fun getAllIncomeDetails(): ResponseEntity<List<IncomeDetailsResponse>> {
        val incomeDetails = incomeDetailsService.getAllIncomeDetails()
        return ResponseEntity.ok(incomeDetails)
    }

    @GetMapping("/{incomeDetailsId}")
    fun getIncomeDetailsById(@PathVariable incomeDetailsId: UUID): ResponseEntity<IncomeDetailsResponse> {
        val incomeDetails = incomeDetailsService.getIncomeDetailsById(incomeDetailsId)
        return ResponseEntity.ok(incomeDetails)
    }

    @PostMapping
    fun createIncomeDetails(@RequestBody incomeDetailsRequest: IncomeDetailsRequest): ResponseEntity<IncomeDetailsResponse> {
        val createdIncomeDetails = incomeDetailsService.createIncomeDetails(incomeDetailsRequest)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdIncomeDetails)
    }

    @PutMapping("/{incomeDetailsId}")
    fun updateIncomeDetails(
        @PathVariable incomeDetailsId: UUID,
        @RequestBody incomeDetailsUpdateRequest: IncomeDetailsUpdateRequest
    ): ResponseEntity<IncomeDetailsResponse> {
        val updatedIncomeDetails = incomeDetailsService.updateIncomeDetails(incomeDetailsId, incomeDetailsUpdateRequest)
        return ResponseEntity.ok(updatedIncomeDetails)
    }

    @DeleteMapping("/{incomeDetailsId}")
    fun deleteIncomeDetails(@PathVariable incomeDetailsId: UUID): ResponseEntity<Void> {
        incomeDetailsService.deleteIncomeDetails(incomeDetailsId)
        return ResponseEntity.noContent().build()
    }
}
