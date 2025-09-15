package com.nivasafinance.features.stages.repository

import com.nivasafinance.features.stages.entity.Stage
import com.nivasafinance.features.stages.exception.StageExceptionFactory
import com.nivasafinance.features.stages.enum.EntityType
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class StageRepositoryWrapper(
    private val stageRepository: StageRepository,
    private val messageSource: MessageSource
) {   

    fun save(stage: Stage): Stage {
        return try {
            stageRepository.save(stage)
        } catch (e: Exception) {
            throw StageExceptionFactory.createFailed(messageSource)
        }
    }

    fun findById(id: UUID): Stage {
        return try {
            stageRepository.findById(id).orElseThrow {
                StageExceptionFactory.notFound(id, messageSource)
            }
        } catch (e: Exception) {
            throw StageExceptionFactory.retrieveEntityFailed(messageSource)
        }
    }

    fun findAllByEntityTypeAndEntityId(entityType: EntityType, entityId: UUID): List<Stage> {
        return try {
            stageRepository.findAllByEntityTypeAndEntityId(entityType, entityId)
        } catch (e: Exception) {
            throw StageExceptionFactory.retrieveEntityFailed(messageSource)
        }
    }

    fun deleteById(id: UUID) {
        return try {
            stageRepository.deleteById(id)
        } catch (e: Exception) {
            throw StageExceptionFactory.deleteFailed(messageSource)
        }
    }

    fun findByStageKey(stageKey: String): Stage {
        return try {
            stageRepository.findByStageKey(stageKey)
        } catch (e: Exception) {
            throw StageExceptionFactory.retrieveEntityFailed(messageSource)
        }
    }
}