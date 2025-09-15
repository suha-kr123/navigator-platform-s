package com.nivasafinance.features.lead.repository

import com.nivasafinance.features.lead.entity.LeadPipelineMapping
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface LeadPipelineMappingRepository : JpaRepository<LeadPipelineMapping, UUID> {
    fun findByLeadId(leadId: UUID): LeadPipelineMapping?
    fun findByPipelineKey(pipelineKey: String): List<LeadPipelineMapping>
    fun findByCurrentStage(currentStage: String): List<LeadPipelineMapping>
}
