package com.nivasafinance.features.lead.repository

import com.nivasafinance.features.lead.dto.LeadSummaryDTO
import com.nivasafinance.features.lead.entity.Lead
import org.javers.spring.annotation.JaversSpringDataAuditable
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
@JaversSpringDataAuditable
interface LeadRepository : JpaRepository<Lead, UUID> {
    
    /**
     * Optimized query for lead summary data - fetches only essential fields and APPLICANT person's primary contact.
     * Uses jsonb_to_recordset for efficient JSON processing and type-safe DTO mapping.
     */
    @Query(
        value = """
        SELECT 
            l.id,
            (l.requested_amount->>'min')::numeric as min_amount,
            (l.requested_amount->>'max')::numeric as max_amount,
            l.purpose,
            l.product_code,
            l.current_stage,
            l.sourcing_channel,
            l.created_at,
            l.created_by,
            l.updated_at,
            l.updated_by,
            pd.person_id,
            p.first_name,
            p.middle_name,
            p.last_name,
            mn.primary_mobile_number,
            pd.lead_person_type
        FROM leads l
        -- Get APPLICANT person data only (optimized with null check)
        LEFT JOIN LATERAL (
            SELECT (pd->>'personId')::uuid as person_id, (pd->>'leadPersonType') as lead_person_type
            FROM jsonb_array_elements(l.person_data) AS pd
            WHERE (pd->>'leadPersonType') = 'APPLICANT'
            LIMIT 1
        ) pd ON l.person_data IS NOT NULL
        LEFT JOIN person p ON pd.person_id = p.id
        -- Get primary mobile number only (optimized with null check)
        LEFT JOIN LATERAL (
            SELECT (mn->>'number') AS primary_mobile_number
            FROM jsonb_array_elements(p.mobile_numbers) AS mn
            WHERE (mn->>'isPrimary')::boolean = true
            LIMIT 1
        ) mn ON p.mobile_numbers IS NOT NULL
        ORDER BY l.created_at DESC
    """,
        nativeQuery = true
    )
    fun findAllSummaryData(pageable: Pageable): Page<LeadSummaryDTO>
    
    /**
     * Optimized count query for leads - faster than counting full entities.
     * Uses a simple count query without joins for maximum performance.
     */
    @Query("SELECT COUNT(l.id) FROM leads l", nativeQuery = true)
    fun countLeads(): Long
}
