package com.nivasafinance.features.tasks.controller

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
    fun getTasksByAssignedTo(@PathVariable assignedTo: String): ResponseEntity<List<TaskResponse>> {
        val taskResponses = taskService.getTasksByAssignedTo(assignedTo)
        return ResponseEntity.ok(taskResponses)
    }
}
