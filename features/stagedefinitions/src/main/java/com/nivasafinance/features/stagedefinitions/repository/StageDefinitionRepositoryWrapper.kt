package com.nivasafinance.features.stagedefinitions.repository

import com.nivasafinance.features.stagedefinitions.entity.StageDefinition
import com.nivasafinance.features.stagedefinitions.exception.StageDefinitionExceptionFactory
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
            throw StageDefinitionExceptionFactory.retrieveFailed(messageSource)
        }
    }

    fun findByKeyWithException(key: String): StageDefinition? {
        return try {
            stageDefinitionRepository.findByKey(key)
        } catch (e: Exception) {
            throw StageDefinitionExceptionFactory.retrieveFailed(messageSource)
        }
    }
}
