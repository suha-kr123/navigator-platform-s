package com.nivasafinance.features.leadpipelinemapping.repository

import com.nivasafinance.features.leadpipelinemapping.entity.LeadPipelineMapping
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface LeadPipelineMappingRepository : JpaRepository<LeadPipelineMapping, UUID> {
    fun findByLeadId(leadId: UUID): LeadPipelineMapping
}
