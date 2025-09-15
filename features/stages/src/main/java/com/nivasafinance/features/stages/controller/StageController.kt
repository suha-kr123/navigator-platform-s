package com.nivasafinance.features.stages.controller

import com.nivasafinance.features.stages.dto.StageResponse
import com.nivasafinance.features.stages.dto.StageUpdateRequest
import com.nivasafinance.features.stages.service.StageService
import com.nivasafinance.features.tasks.dto.TaskRequest
import com.nivasafinance.features.tasks.dto.TaskResponse
import com.nivasafinance.features.tasks.dto.UpdateTaskRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/stages")
class StageController(
    private val stageService: StageService
) {

    @PostMapping("/{stageId}/tasks")
    fun createTaskForStage(
        @PathVariable stageId: UUID,
        @RequestBody taskRequest: TaskRequest
    ): ResponseEntity<TaskResponse> {
        val task = stageService.addTasksToStage(stageId, listOf(taskRequest))
        return ResponseEntity.status(HttpStatus.CREATED).body(task.first())
    }

    @PutMapping("/{stageId}")
    fun updateStage(
        @PathVariable stageId: UUID,
        @RequestBody stageUpdateRequest: StageUpdateRequest
    ): ResponseEntity<StageResponse> {
        val stageResponse = stageService.updateStage(stageId, stageUpdateRequest)
        return ResponseEntity.ok(stageResponse)
    }

    @PutMapping("/{stageId}/tasks/{taskId}")
    fun updateTaskForStage(
        @PathVariable stageId: UUID,
        @PathVariable taskId: UUID,
        @RequestBody updateTaskRequest: UpdateTaskRequest
    ): ResponseEntity<TaskResponse> {
        val taskResponse = stageService.updateTaskInStage(stageId, taskId, updateTaskRequest)
        return ResponseEntity.ok(taskResponse)
    }

    @DeleteMapping("/{stageId}/tasks/{taskId}")
    fun deleteTaskForStage(
        @PathVariable stageId: UUID,
        @PathVariable taskId: UUID
    ): ResponseEntity<Void> {
        stageService.deleteTaskFromStage(stageId, taskId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{stageId}/tasks")
    fun getAllTasksForStage(@PathVariable stageId: UUID): ResponseEntity<List<TaskResponse>> {
        val tasks = stageService.getTasksForStage(stageId)
        return ResponseEntity.ok(tasks)
    }
}
