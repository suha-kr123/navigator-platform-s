package com.nivasafinance.features.tasks.controller

import base.model.PaginatedResponse
import base.model.PaginationRequest
import base.model.SortDirection
import com.nivasafinance.features.tasks.dto.TaskRequest
import com.nivasafinance.features.tasks.dto.TaskResponse
import com.nivasafinance.features.tasks.dto.UpdateTaskRequest
import com.nivasafinance.features.tasks.service.TaskService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/tasks")
class TaskController(
    private val taskService: TaskService
) {

    @GetMapping("/{entityType}/{entityId}")
    fun getTasksByEntity(
        @PathVariable entityType: String,
        @PathVariable entityId: UUID
    ): ResponseEntity<List<TaskResponse>> {
        val tasks = taskService.getTasksByEntity(entityType, entityId)
        return ResponseEntity.ok(tasks)
    }

    @PostMapping("/{entityType}/{entityId}")
    fun createTaskByEntity(
        @PathVariable entityType: String,
        @PathVariable entityId: UUID,
        @RequestBody taskRequest: TaskRequest
    ): ResponseEntity<TaskResponse> {
        val createdTask = taskService.createTaskByEntity(entityType, entityId, taskRequest)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask)
    }

    @PutMapping("/{entityType}/{entityId}/{taskId}")
    fun updateTaskByEntity(
        @PathVariable entityType: String,
        @PathVariable entityId: UUID,
        @PathVariable taskId: UUID,
        @RequestBody taskRequest: UpdateTaskRequest
    ): ResponseEntity<TaskResponse> {
        val updatedTask = taskService.updateTaskByEntity(entityType, entityId, taskId, taskRequest)
        return ResponseEntity.ok(updatedTask)
    }

    @PostMapping("/{entityType}/batch")
    fun getTasksByEntityTypeAndEntityIds(
        @PathVariable entityType: String,
        @RequestBody entityIds: List<UUID>,
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
        val taskResponses = taskService.getTasksByEntityTypeAndEntityIds(entityType, entityIds, paginationRequest)
        return ResponseEntity.ok(taskResponses)
    }
}
