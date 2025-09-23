package com.nivasafinance.features.taskdefinitions.controller

import com.nivasafinance.features.taskdefinitions.dto.TaskOutcomesResponse
import com.nivasafinance.features.taskdefinitions.service.TaskDefinitionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/task-definitions")
class TaskDefinitionController(private val taskDefinitionService: TaskDefinitionService) {

    @GetMapping("/{key}/outcomes")
    fun getTaskOutcomesByKey(
        @PathVariable key: String
    ): ResponseEntity<TaskOutcomesResponse> {
        val taskOutcomes = taskDefinitionService.getTaskOutcomesByKey(key)
        return ResponseEntity.ok(taskOutcomes)
    }
}
