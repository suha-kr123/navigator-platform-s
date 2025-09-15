package com.nivasafinance.features.tasks.controller

import base.model.PaginationRequest
import base.model.PaginatedResponse
import base.model.SortDirection
import com.nivasafinance.features.tasks.dto.TaskResponse
import com.nivasafinance.features.tasks.service.TaskService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/tasks")
class TaskController(
    private val taskService: TaskService
) {

    @GetMapping("/assigned/{assignedTo}")
    fun getTasksByAssignedTo(
        @PathVariable assignedTo: String,
        @RequestParam(defaultValue = "0") offset: Int,
        @RequestParam(defaultValue = "20") limit: Int,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "ASC") sortDirection: String
    ): ResponseEntity<PaginatedResponse<TaskResponse>> {
        val paginationRequest = PaginationRequest(
            offset = offset,
            limit = limit,
            sortBy = sortBy,
            sortDirection = SortDirection.valueOf(sortDirection.uppercase())
        )
        val taskResponses = taskService.getTasksByAssignedTo(assignedTo, paginationRequest)
        return ResponseEntity.ok(taskResponses)
    }
}
