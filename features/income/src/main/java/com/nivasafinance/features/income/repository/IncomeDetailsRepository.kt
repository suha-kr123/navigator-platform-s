package com.nivasafinance.features.income.repository

import com.nivasafinance.features.income.entity.IncomeDetails
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface IncomeDetailsRepository : JpaRepository<IncomeDetails, UUID> {
    
    fun findAllByEntityTypeAndEntityId(entityType: String, entityId: UUID): List<IncomeDetails>
}
