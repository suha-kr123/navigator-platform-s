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

    @GetMapping("/{entityType}/{entityId}")
    fun getIncomeDetailsByEntity(
        @PathVariable entityType: String,
        @PathVariable entityId: UUID,
    ): ResponseEntity<List<IncomeDetailsResponse>> {
        val incomeDetails = incomeDetailsService.getIncomeDetailsByEntity(entityType, entityId)
        return ResponseEntity.ok(incomeDetails)
    }

    @PostMapping("/{entityType}/{entityId}")
    fun createIncomeDetails(
        @PathVariable entityType: String,
        @PathVariable entityId: UUID,
        @RequestBody incomeDetailsRequest: IncomeDetailsRequest
    ): ResponseEntity<IncomeDetailsResponse> {
        val createdIncomeDetails = incomeDetailsService.createIncomeDetailsByEntity(entityType, entityId, incomeDetailsRequest)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdIncomeDetails)
    }

    @PutMapping("/{entityType}/{entityId}/{incomeDetailsId}")
    fun updateIncomeDetails(
        @PathVariable entityType: String,
        @PathVariable entityId: UUID,
        @PathVariable incomeDetailsId: UUID,
        @RequestBody incomeDetailsUpdateRequest: IncomeDetailsUpdateRequest
    ): ResponseEntity<IncomeDetailsResponse> {
        val updatedIncomeDetails = incomeDetailsService.updateIncomeDetailsByEntity(entityType, entityId, incomeDetailsId, incomeDetailsUpdateRequest)
        return ResponseEntity.ok(updatedIncomeDetails)
    }

    @DeleteMapping("/{entityType}/{entityId}/{incomeDetailsId}")
    fun deleteIncomeDetails(
        @PathVariable entityType: String,
        @PathVariable entityId: UUID,
        @PathVariable incomeDetailsId: UUID
    ): ResponseEntity<Void> {
        incomeDetailsService.deleteIncomeDetailsByEntity(entityType, entityId, incomeDetailsId)
        return ResponseEntity.noContent().build()
    }
}
