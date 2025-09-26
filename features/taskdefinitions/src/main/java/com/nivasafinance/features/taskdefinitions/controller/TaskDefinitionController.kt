package com.nivasafinance.features.taskdefinitions.controller

import com.nivasafinance.features.taskdefinitions.dto.TaskDefinitionListResponse
import com.nivasafinance.features.taskdefinitions.dto.TaskOutcomesResponse
import com.nivasafinance.features.taskdefinitions.service.TaskDefinitionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/task-definitions")
class TaskDefinitionController(
    private val taskDefinitionService: TaskDefinitionService
) {

    @GetMapping
    fun getAllTaskDefinitions(): ResponseEntity<TaskDefinitionListResponse> {
        val response = taskDefinitionService.getAllTaskDefinitions()
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{key}/outcomes")
    fun getTaskOutcomesByKey(@PathVariable key: String): ResponseEntity<TaskOutcomesResponse> {
        val response = taskDefinitionService.getTaskOutcomesByKey(key)
        return ResponseEntity.ok(response)
    }
}
