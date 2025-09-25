package com.nivasafinance.features.lead.controller

import base.model.PaginatedResponse
import base.model.PaginationRequest
import com.nivasafinance.features.lead.dto.LeadTasksResponse
import com.nivasafinance.features.lead.service.LeadTaskService
import com.nivasafinance.features.tasks.dto.TaskRequest
import com.nivasafinance.features.tasks.dto.UpdateTaskRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/leads")
class LeadTaskController(
    private val leadTaskService: LeadTaskService
) {

    @GetMapping("/tasks")
    fun getAllTasks(
        @RequestParam(defaultValue = "0") offset: Int,
        @RequestParam(defaultValue = "20") limit: Int,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "ASC") sortDirection: String
    ): ResponseEntity<PaginatedResponse<LeadTasksResponse>> {
        val paginationRequest = PaginationRequest(
            offset = offset,
            limit = limit,
            sortBy = sortBy,
            sortDirection = sortDirection
        )
        val leadTasks = leadTaskService.getAllTasks(paginationRequest)
        return ResponseEntity.ok(leadTasks)
    }

    @GetMapping("/{leadId}/tasks")
    fun getLeadTasks(
        @PathVariable leadId: UUID,
        @RequestParam(defaultValue = "0") offset: Int,
        @RequestParam(defaultValue = "20") limit: Int,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "ASC") sortDirection: String
    ): ResponseEntity<PaginatedResponse<LeadTasksResponse>> {
        val paginationRequest = PaginationRequest(
            offset = offset,
            limit = limit,
            sortBy = sortBy,
            sortDirection = sortDirection
        )

        val leadTasks = leadTaskService.getLeadTasks(leadId, paginationRequest)
        return ResponseEntity.ok(leadTasks)
    }

    @PostMapping("/{leadId}/tasks")
    fun createTaskForLead(
        @PathVariable leadId: UUID,
        @RequestBody createTaskRequest: TaskRequest
    ): ResponseEntity<LeadTasksResponse> {
        val leadTask = leadTaskService.createTaskForLead(leadId, createTaskRequest)
        return ResponseEntity.status(HttpStatus.CREATED).body(leadTask)
    }

    @PatchMapping("/{leadId}/tasks/{taskId}")
    fun patchTaskForLead(
        @PathVariable leadId: UUID,
        @PathVariable taskId: UUID,
        @RequestBody updateTaskRequest: UpdateTaskRequest
    ): ResponseEntity<LeadTasksResponse> {
        val updatedTask = leadTaskService.patchTaskForLead(leadId, taskId, updateTaskRequest)
        return ResponseEntity.ok(updatedTask)
    }

    @DeleteMapping("/{leadId}/tasks/{taskId}")
    fun deleteTaskForLead(
        @PathVariable leadId: UUID,
        @PathVariable taskId: UUID
    ): ResponseEntity<Unit> {
        leadTaskService.deleteTaskForLead(leadId, taskId)
        return ResponseEntity.noContent().build()
    }
}
