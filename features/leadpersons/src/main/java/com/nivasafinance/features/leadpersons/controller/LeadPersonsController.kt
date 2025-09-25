package com.nivasafinance.features.leadpersons.controller

import base.model.PaginatedResponse
import base.model.PaginationRequest
import com.nivasafinance.features.leadpersons.dto.LeadPersonRequest
import com.nivasafinance.features.leadpersons.dto.LeadPersonResponse
import com.nivasafinance.features.leadpersons.service.LeadPersonsService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/lead-persons")
class LeadPersonsController(
    private val leadPersonsService: LeadPersonsService
) {

    @PostMapping
    fun createLeadPerson(@RequestBody leadPersonRequest: LeadPersonRequest): ResponseEntity<LeadPersonResponse> {
        val createdLeadPerson = leadPersonsService.createLeadPerson(leadPersonRequest)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdLeadPerson)
    }

    @GetMapping("/{id}")
    fun getLeadPersonById(@PathVariable id: UUID): ResponseEntity<LeadPersonResponse> {
        val leadPerson = leadPersonsService.getLeadPersonById(id)
        return ResponseEntity.ok(leadPerson)
    }

    @GetMapping
    fun getAllLeadPersons(
        @RequestParam(defaultValue = "0") offset: Int,
        @RequestParam(defaultValue = "20") limit: Int,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "ASC") sortDirection: String
    ): ResponseEntity<PaginatedResponse<LeadPersonResponse>> {
        val paginationRequest = PaginationRequest(
            offset = offset,
            limit = limit,
            sortBy = sortBy,
            sortDirection = sortDirection
        )
        val leadPersons = leadPersonsService.getAllLeadPersons(paginationRequest)
        return ResponseEntity.ok(leadPersons)
    }

    @GetMapping("/lead/{leadId}")
    fun getLeadPersonsByLeadId(@PathVariable leadId: UUID): ResponseEntity<List<LeadPersonResponse>> {
        val leadPersons = leadPersonsService.getLeadPersonsByLeadId(leadId)
        return ResponseEntity.ok(leadPersons)
    }

    @GetMapping("/person/{personId}")
    fun getLeadPersonsByPersonId(@PathVariable personId: UUID): ResponseEntity<List<LeadPersonResponse>> {
        val leadPersons = leadPersonsService.getLeadPersonsByPersonId(personId)
        return ResponseEntity.ok(leadPersons)
    }

    @PutMapping("/{id}")
    fun updateLeadPerson(
        @PathVariable id: UUID,
        @RequestBody leadPersonRequest: LeadPersonRequest
    ): ResponseEntity<LeadPersonResponse> {
        val updatedLeadPerson = leadPersonsService.updateLeadPerson(id, leadPersonRequest)
        return ResponseEntity.ok(updatedLeadPerson)
    }

    @DeleteMapping("/{id}")
    fun deleteLeadPerson(@PathVariable id: UUID): ResponseEntity<Void> {
        leadPersonsService.deleteLeadPerson(id)
        return ResponseEntity.noContent().build()
    }
}
