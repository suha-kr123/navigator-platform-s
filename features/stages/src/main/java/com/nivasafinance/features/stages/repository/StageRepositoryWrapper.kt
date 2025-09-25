package com.nivasafinance.features.stages.repository

import com.nivasafinance.features.stages.entity.Stage
import com.nivasafinance.features.stages.exception.StageExceptionFactory
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class StageRepositoryWrapper(
    private val stageRepository: StageRepository,
    private val messageSource: MessageSource
) {

    fun saveWithException(stage: Stage): Stage {
        return try {
            stageRepository.save(stage)
        } catch (e: Exception) {
            throw StageExceptionFactory.createFailed(messageSource)
        }
    }

    fun findAllWithException(): List<Stage> {
        return try {
            stageRepository.findAll()
        } catch (e: Exception) {
            throw StageExceptionFactory.retrieveEntityFailed(messageSource)
        }
    }

    fun findByIdWithException(id: UUID): Stage {
        return try {
            stageRepository.findById(id).orElseThrow {
                StageExceptionFactory.notFound(id, messageSource)
            }
        } catch (e: Exception) {
            throw StageExceptionFactory.retrieveEntityFailed(messageSource)
        }
    }

    fun findAllByStageDefinitionKeyWithException(stageDefinitionKey: String): List<Stage> {
        return try {
            stageRepository.findAllByStageDefinitionKey(stageDefinitionKey)
        } catch (e: Exception) {
            throw StageExceptionFactory.retrieveEntityFailed(messageSource)
        }
    }

    fun deleteByIdWithException(id: UUID) {
        return try {
            stageRepository.deleteById(id)
        } catch (e: Exception) {
            throw StageExceptionFactory.deleteFailed(messageSource)
        }
    }

    fun existsByStageDefinitionKeyWithException(stageDefinitionKey: String): Boolean {
        return try {
            stageRepository.existsByStageDefinitionKey(stageDefinitionKey)
        } catch (e: Exception) {
            throw StageExceptionFactory.retrieveEntityFailed(messageSource)
        }
    }
}
