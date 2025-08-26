package com.nivasafinance.features.advisorleadmapping.repository

import com.nivasafinance.features.advisorleadmapping.entity.AdvisorLeadMapping
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface AdvisorLeadMappingRepository : JpaRepository<AdvisorLeadMapping, UUID> {

    fun findByAdvisorId(advisorId: UUID, pageable: Pageable): Page<AdvisorLeadMapping>

    fun findByAdvisorId(advisorId: UUID): List<AdvisorLeadMapping>

    fun findByLeadId(leadId: UUID, pageable: Pageable): Page<AdvisorLeadMapping>

    fun findByAdvisorIdAndLeadId(advisorId: UUID, leadId: UUID): AdvisorLeadMapping?

    fun existsByAdvisorIdAndLeadId(advisorId: UUID, leadId: UUID): Boolean
}
