package com.nivasafinance.features.taskdefinitions.controller

import com.nivasafinance.features.taskdefinitions.dto.TaskDefinitionResponse
import com.nivasafinance.features.taskdefinitions.dto.TaskOutcomesResponse
import com.nivasafinance.features.taskdefinitions.service.TaskDefinitionService
import com.nivasafinance.features.tasks.enum.TaskStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/task-definitions")
class TaskDefinitionController(
    private val taskDefinitionService: TaskDefinitionService
) {

    @GetMapping
    fun getAllTaskDefinitions(): ResponseEntity<List<TaskDefinitionResponse>> {
        val taskDefinitions = taskDefinitionService.getAllTaskDefinitions()
        return ResponseEntity.ok(taskDefinitions)
    }

    @GetMapping("/{key}/outcomes")
    fun getTaskOutcomesByKey(
        @PathVariable key: String,
        @RequestParam(required = false) status: TaskStatus?
    ): ResponseEntity<TaskOutcomesResponse> {
        val response = taskDefinitionService.getTaskOutcomesByKey(key, status)
        return ResponseEntity.ok(response)
    }
}
