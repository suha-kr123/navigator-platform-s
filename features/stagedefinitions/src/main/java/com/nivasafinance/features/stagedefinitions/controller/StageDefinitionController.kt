package com.nivasafinance.features.stagedefinitions.controller

import com.nivasafinance.features.stagedefinitions.dto.StageOutcomesResponse
import com.nivasafinance.features.stagedefinitions.service.StageDefinitionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/stage-definitions")
class StageDefinitionController(private val stageDefinitionService: StageDefinitionService) {

    @GetMapping("/{key}/outcomes")
    fun getStageOutcomesByKey(
        @PathVariable key: String
    ): ResponseEntity<StageOutcomesResponse> {
        val stageOutcomes = stageDefinitionService.getStageOutcomesByKey(key)
        return ResponseEntity.ok(stageOutcomes)
    }
}
