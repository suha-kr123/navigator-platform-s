package com.nivasafinance.features.stages.controller

import com.nivasafinance.features.stages.dto.StageRequest
import com.nivasafinance.features.stages.dto.StageResponse
import com.nivasafinance.features.stages.dto.StageUpdateRequest
import com.nivasafinance.features.stages.service.StageService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/stages")
class StageController(
    private val stageService: StageService
) {

    @PostMapping
    fun createStage(
        @RequestBody stageRequest: StageRequest
    ): ResponseEntity<StageResponse> {
        val createdStage = stageService.createStage(stageRequest)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdStage)
    }

    @GetMapping
    fun getAllStages(): ResponseEntity<List<StageResponse>> {
        val stages = stageService.getAllStages()
        return ResponseEntity.ok(stages)
    }

    @GetMapping("/{stageId}")
    fun getStageById(
        @PathVariable stageId: UUID
    ): ResponseEntity<StageResponse> {
        val stage = stageService.getStageById(stageId)
        return ResponseEntity.ok(stage)
    }

    @GetMapping("/definition/{stageDefinitionKey}")
    fun getStagesByDefinitionKey(
        @PathVariable stageDefinitionKey: String
    ): ResponseEntity<List<StageResponse>> {
        val stages = stageService.getStagesByDefinitionKey(stageDefinitionKey)
        return ResponseEntity.ok(stages)
    }

    @PutMapping("/{stageId}")
    fun updateStage(
        @PathVariable stageId: UUID,
        @RequestBody stageUpdateRequest: StageUpdateRequest
    ): ResponseEntity<StageResponse> {
        val updatedStage = stageService.updateStage(stageId, stageUpdateRequest)
        return ResponseEntity.ok(updatedStage)
    }
}
