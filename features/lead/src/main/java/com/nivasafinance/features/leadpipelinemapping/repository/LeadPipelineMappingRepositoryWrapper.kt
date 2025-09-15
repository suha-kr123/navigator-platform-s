package com.nivasafinance.features.leadpipelinemapping.repository

import com.nivasafinance.features.leadpipelinemapping.entity.LeadPipelineMapping
import com.nivasafinance.features.leadpipelinemapping.exception.LeadPipelineMappingExceptionFactory
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class LeadPipelineMappingRepositoryWrapper(
    private val leadPipelineMappingRepository: LeadPipelineMappingRepository,
    private val messageSource: MessageSource
) {

    fun save(leadPipelineMapping: LeadPipelineMapping): LeadPipelineMapping {
        return try {
            leadPipelineMappingRepository.save(leadPipelineMapping)
        } catch (e: Exception) {
            throw LeadPipelineMappingExceptionFactory.createFailed(messageSource)
        }
    }

    fun findByLeadId(leadId: UUID): LeadPipelineMapping {
        return try {
            leadPipelineMappingRepository.findByLeadId(leadId)
        } catch (e: Exception) {
            throw LeadPipelineMappingExceptionFactory.retrieveEntityFailed(messageSource)
        }
    }
}