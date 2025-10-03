package com.nivasafinance.features.stagedefinitions.dto

data class StageOutcomesResponse(
    val stageDefinitionKey: String,
    val stageDefinitionName: String,
    val outcomes: List<String>
)
