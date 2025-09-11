package com.nivasafinance.features.income.repository

import com.nivasafinance.features.income.entity.IncomeDetails
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface IncomeDetailsRepository : JpaRepository<IncomeDetails, UUID> {

    fun findByEntityIdAndEntityType(entityId: UUID, entityType: String): List<IncomeDetails>

    fun findByEntityIdAndEntityTypeAndEmploymentType(entityId: UUID, entityType: String, employmentType: String): List<IncomeDetails>

    fun findByEntityIdAndEntityTypeAndIsPrimary(entityId: UUID, entityType: String, isPrimary: Boolean): List<IncomeDetails>

    fun existsByEntityIdAndEntityTypeAndEmploymentType(entityId: UUID, entityType: String, employmentType: String): Boolean

    @Query("SELECT i FROM IncomeDetails i WHERE i.entityType = :entityType AND i.employmentType = :employmentType")
    fun findByEntityTypeAndEmploymentType(
        @Param("entityType") entityType: String,
        @Param("employmentType") employmentType: String
    ): List<IncomeDetails>
}
