package com.nivasafinance.features.stagedefinitions.service

import com.nivasafinance.features.stagedefinitions.dto.StageOutcomesResponse
import com.nivasafinance.features.stagedefinitions.exception.StageDefinitionExceptionFactory
import com.nivasafinance.features.stagedefinitions.repository.StageDefinitionRepositoryWrapper
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service

@Service
class StageDefinitionServiceImpl(
    private val stageDefinitionRepositoryWrapper: StageDefinitionRepositoryWrapper,
    private val messageSource: MessageSource
) : StageDefinitionService {

    override fun getStageOutcomesByKey(key: String): StageOutcomesResponse {
        val stageDefinition = stageDefinitionRepositoryWrapper.findByKeyWithException(key)
            ?: throw StageDefinitionExceptionFactory.notFound(key, messageSource)
        
        val outcomes = stageDefinition.possibleOutcomes ?: emptyList()
        
        return StageOutcomesResponse(
            stageDefinitionKey = stageDefinition.key,
            stageDefinitionName = stageDefinition.name,
            outcomes = outcomes
        )
    }
}
