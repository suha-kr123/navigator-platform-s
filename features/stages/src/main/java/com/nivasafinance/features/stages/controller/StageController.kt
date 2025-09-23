package com.nivasafinance.features.stages.controller

import base.model.PaginatedResponse
import base.model.PaginationRequest
import base.model.SortDirection
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

    @PostMapping("/{entityType}/{entityId}")
    fun createStageByEntity(
        @PathVariable entityType: String,
        @PathVariable entityId: UUID,
        @RequestBody stageRequest: StageRequest
    ): ResponseEntity<StageResponse> {
        val createdStage = stageService.createStageByEntity(entityType, entityId, stageRequest)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdStage)
    }

    @GetMapping("/{entityType}/{entityId}")
    fun getStagesByEntity(
        @PathVariable entityType: String,
        @PathVariable entityId: UUID
    ): ResponseEntity<List<StageResponse>> {
        val stages = stageService.getStagesByEntity(entityType, entityId)
        return ResponseEntity.ok(stages)
    }

    @PutMapping("/{entityType}/{entityId}/{stageId}")
    fun updateStageByEntity(
        @PathVariable entityType: String,
        @PathVariable entityId: UUID,
        @PathVariable stageId: UUID,
        @RequestBody stageUpdateRequest: StageUpdateRequest
    ): ResponseEntity<StageResponse> {
        val updatedStage = stageService.updateStageByEntity(entityType, entityId, stageId, stageUpdateRequest)
        return ResponseEntity.ok(updatedStage)
    }
}
