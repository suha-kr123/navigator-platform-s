package com.nivasafinance.features.stagedefinitions.dto

import java.time.LocalDateTime

data class StageDefinitionResponse(
    val key: String,
    val name: String,
    val description: String?,
    val pipelineKey: String,
    val outcomes: List<String>,
    val extData: Map<String, Any>? = null,
    val createdAt: LocalDateTime,
    val createdBy: String?,
    val updatedAt: LocalDateTime,
    val updatedBy: String?,
)
