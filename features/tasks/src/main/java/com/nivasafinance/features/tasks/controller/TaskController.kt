package com.nivasafinance.features.tasks.controller

import base.model.PaginatedResponse
import base.model.PaginationRequest
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

    @GetMapping("/{taskId}")
    fun getTaskById(@PathVariable taskId: UUID): ResponseEntity<TaskResponse> {
        val task = taskService.getTaskById(taskId)
        return ResponseEntity.ok(task)
    }

    @GetMapping
    fun getAllTasks(
        @RequestParam(defaultValue = "0") offset: Int,
        @RequestParam(defaultValue = "20") limit: Int,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "ASC") sortDirection: String
    ): ResponseEntity<PaginatedResponse<TaskResponse>> {
        val paginationRequest = PaginationRequest(
            offset = offset,
            limit = limit,
            sortBy = sortBy,
            sortDirection = sortDirection
        )
        val tasks = taskService.getAllTasks(paginationRequest)
        return ResponseEntity.ok(tasks)
    }

    @PostMapping
    fun createTask(@RequestBody taskRequest: TaskRequest): ResponseEntity<TaskResponse> {
        val createdTask = taskService.createTask(taskRequest)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask)
    }

    @PutMapping("/{taskId}")
    fun updateTaskById(
        @PathVariable taskId: UUID,
        @RequestBody taskRequest: UpdateTaskRequest
    ): ResponseEntity<TaskResponse> {
        val updatedTask = taskService.updateTaskById(taskId, taskRequest)
        return ResponseEntity.ok(updatedTask)
    }

    @PatchMapping("/{taskId}")
    fun patchTaskById(
        @PathVariable taskId: UUID,
        @RequestBody taskRequest: UpdateTaskRequest
    ): ResponseEntity<TaskResponse> {
        val updatedTask = taskService.patchTaskById(taskId, taskRequest)
        return ResponseEntity.ok(updatedTask)
    }

    @DeleteMapping("/{taskId}")
    fun deleteTaskById(@PathVariable taskId: UUID): ResponseEntity<Unit> {
        taskService.deleteTaskById(taskId)
        return ResponseEntity.noContent().build()
    }
}

