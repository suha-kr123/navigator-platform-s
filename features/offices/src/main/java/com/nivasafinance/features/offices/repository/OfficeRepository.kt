package com.nivasafinance.features.offices.repository

import com.nivasafinance.features.offices.entity.Office
import org.javers.spring.annotation.JaversSpringDataAuditable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
@JaversSpringDataAuditable
interface OfficeRepository : JpaRepository<Office, UUID> {

    @Query("SELECT o FROM Office o WHERE o.parentId IS NULL ORDER BY o.code DESC")
    fun findByParentIdIsNullOrderByCodeDesc(): List<Office>

    @Query("SELECT o FROM Office o WHERE o.parentId = :parentId ORDER BY o.code DESC")
    fun findByParentIdOrderByCodeDesc(parentId: UUID): List<Office>
}
