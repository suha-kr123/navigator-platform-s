package com.nivasafinance.features.lead.controller

import base.model.PaginatedResponse
import base.model.PaginationRequest
import com.nivasafinance.features.lead.dto.CreateTaskForLeadRequest
import com.nivasafinance.features.lead.dto.LeadCreateRequest
import com.nivasafinance.features.lead.dto.LeadResponse
import com.nivasafinance.features.lead.dto.LeadTaskResponse
import com.nivasafinance.features.lead.service.LeadService
import com.nivasafinance.features.tasks.service.TaskService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/leads")
class LeadController(
    private val leadService: LeadService,
    private val taskService: TaskService
) {

    @PostMapping
    fun createLead(@RequestBody leadCreateRequest: LeadCreateRequest): ResponseEntity<LeadResponse> {
        val lead = leadService.createLead(leadCreateRequest)
        return ResponseEntity.status(HttpStatus.CREATED).body(lead)
    }

    @PostMapping("/{leadId}/tasks")
    fun createTaskForLead(
        @PathVariable leadId: UUID,
        @RequestBody createTaskForLeadRequest: CreateTaskForLeadRequest
    ): ResponseEntity<LeadTaskResponse> {
        val leadTask = leadService.createTaskForLead(leadId, createTaskForLeadRequest)
        return ResponseEntity.status(HttpStatus.CREATED).body(leadTask)
    }

    @GetMapping("/{id}")
    fun getLeadById(@PathVariable id: UUID): ResponseEntity<LeadResponse> {
        val lead = leadService.getLeadById(id)
        return ResponseEntity.ok(lead)
    }

    @GetMapping
    fun getAllLeads(
        @RequestParam(defaultValue = "0") offset: Int,
        @RequestParam(defaultValue = "20") limit: Int,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "ASC") sortDirection: String
    ): ResponseEntity<PaginatedResponse<LeadResponse>> {
        val paginationRequest = PaginationRequest(
            offset = offset,
            limit = limit,
            sortBy = sortBy,
            sortDirection = sortDirection
        )
        val leads = leadService.getAllLeads(paginationRequest)
        return ResponseEntity.ok(leads)
    }

    @GetMapping("/{id}/tasks")
    fun getLeadTasks(
        @PathVariable id: UUID,
        @RequestParam(defaultValue = "0") offset: Int,
        @RequestParam(defaultValue = "20") limit: Int,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "ASC") sortDirection: String
    ): ResponseEntity<PaginatedResponse<LeadTaskResponse>> {
        val paginationRequest = PaginationRequest(
            offset = offset,
            limit = limit,
            sortBy = sortBy,
            sortDirection = sortDirection
        )
        val tasks = leadService.getLeadTasks(id, paginationRequest)
        return ResponseEntity.ok(tasks)
    }

    @GetMapping("/tasks")
    fun getAllLeadsTasks(
        @RequestParam(defaultValue = "0") offset: Int,
        @RequestParam(defaultValue = "20") limit: Int,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "ASC") sortDirection: String
    ): ResponseEntity<PaginatedResponse<LeadTaskResponse>> {
        val paginationRequest = PaginationRequest(
            offset = offset,
            limit = limit,
            sortBy = sortBy,
            sortDirection = sortDirection
        )
        val tasks = leadService.getAllLeadsTasks(paginationRequest)
        return ResponseEntity.ok(tasks)
    }
}
