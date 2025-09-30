package com.nivasafinance.features.leadlender.repository

import com.nivasafinance.features.leadlender.entity.LeadLender
import com.nivasafinance.features.leadlender.enum.LeadLenderStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface LeadLenderRepository : JpaRepository<LeadLender, UUID> {
    fun findByLeadId(leadId: UUID): List<LeadLender>
    fun findByLenderKey(lenderKey: String): List<LeadLender>
    fun findByStatus(status: LeadLenderStatus): List<LeadLender>
    fun findByLeadIdAndLenderKey(leadId: UUID, lenderKey: String): LeadLender?
    fun findByLeadIdAndStatus(leadId: UUID, status: LeadLenderStatus): List<LeadLender>

    @Query("SELECT ll FROM LeadLender ll WHERE ll.leadId = :leadId AND ll.status = :status ORDER BY ll.createdAt DESC")
    fun findByLeadIdAndStatusOrderByCreatedAtDesc(
        @Param("leadId") leadId: UUID,
        @Param("status") status: LeadLenderStatus
    ): List<LeadLender>
}
