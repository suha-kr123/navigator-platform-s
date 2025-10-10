package com.nivasafinance.features.lead.controller

import com.nivasafinance.common.base.model.PaginatedResponse
import com.nivasafinance.common.base.model.PaginationRequest
import com.nivasafinance.features.lead.dto.LeadTasksResponse
import com.nivasafinance.features.lead.service.LeadTaskService
import com.nivasafinance.features.tasks.dto.TaskRequest
import com.nivasafinance.features.tasks.dto.UpdateTaskRequest
import com.nivasafinance.features.taskhistory.dto.TaskHistoryResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/leads")
class LeadTaskController(
    private val leadTaskService: LeadTaskService
) {

    @GetMapping("/tasks/all")
    fun getAllTasks(
        @RequestParam(defaultValue = "0") offset: Int,
        @RequestParam(defaultValue = "20") limit: Int,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "ASC") sortDirection: String
    ): ResponseEntity<PaginatedResponse<List<LeadTasksResponse>>> {
        val paginationRequest = PaginationRequest(
            offset = offset,
            limit = limit,
            sortBy = sortBy,
            sortDirection = sortDirection
        )

        val tasks = leadTaskService.getAllTasks(paginationRequest)
        return ResponseEntity.ok(tasks)
    }

    @GetMapping("/{leadId}/tasks/all")
    fun getLeadTasks(
        @PathVariable leadId: UUID,
        @RequestParam(defaultValue = "0") offset: Int,
        @RequestParam(defaultValue = "20") limit: Int,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "ASC") sortDirection: String
    ): ResponseEntity<PaginatedResponse<List<LeadTasksResponse>>> {
        val paginationRequest = PaginationRequest(
            offset = offset,
            limit = limit,
            sortBy = sortBy,
            sortDirection = sortDirection
        )

        val leadTasks = leadTaskService.getLeadTasks(leadId, paginationRequest)
        return ResponseEntity.ok(leadTasks)
    }

    @GetMapping("/{leadId}/tasks/{taskId}")
    fun getTaskForLead(
        @PathVariable leadId: UUID,
        @PathVariable taskId: UUID
    ): ResponseEntity<LeadTasksResponse> {
        val leadTask = leadTaskService.getTaskForLead(leadId, taskId)
        return ResponseEntity.ok(leadTask)
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

    @GetMapping("/{leadId}/tasks/{taskId}/history")
    fun getTaskHistoryForLead(
        @PathVariable leadId: UUID,
        @PathVariable taskId: UUID,
        @RequestParam(defaultValue = "0") offset: Int,
        @RequestParam(defaultValue = "20") limit: Int,
        @RequestParam(defaultValue = "changedAt") sortBy: String,
        @RequestParam(defaultValue = "ASC") sortDirection: String
    ): ResponseEntity<PaginatedResponse<TaskHistoryResponse>> {
        val paginationRequest = PaginationRequest(
            offset = offset,
            limit = limit,
            sortBy = sortBy,
            sortDirection = sortDirection
        )

        val taskHistory = leadTaskService.getTaskHistoryForLead(leadId, taskId, paginationRequest)
        return ResponseEntity.ok(taskHistory)
    }
}
