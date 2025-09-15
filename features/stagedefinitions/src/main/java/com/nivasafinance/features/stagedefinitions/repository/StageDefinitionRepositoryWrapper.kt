package com.nivasafinance.features.stagedefinitions.repository

import com.nivasafinance.features.stagedefinitions.entity.StageDefinition
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service

@Service
class StageDefinitionRepositoryWrapper(
    private val stageDefinitionRepository: StageDefinitionRepository,
    private val messageSource: MessageSource
) {

    fun findByPipelineKeyWithException(pipelineKey: String): List<StageDefinition> {
        return try {
            stageDefinitionRepository.findByPipelineKeyOrderByKeyAsc(pipelineKey)
        } catch (e: Exception) {
            throw RuntimeException("Failed to retrieve stage definitions for pipeline: $pipelineKey", e)
        }
    }
}
