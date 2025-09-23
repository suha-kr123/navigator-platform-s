package com.nivasafinance.features.advisorleadmapping.repository

import com.nivasafinance.features.advisorleadmapping.entity.AdvisorLeadMapping
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface AdvisorLeadMappingRepository : JpaRepository<AdvisorLeadMapping, UUID> {
    fun findAllByAdvisorId(advisorId: UUID, pageable: Pageable): Page<AdvisorLeadMapping>
}
