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
     * Returns one row per lead with the APPLICANT person's primary contact information.
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
            mn.primary_mobile_number as mobile_number,
            pd.lead_person_type,
            pd.relationship_to_primary
        FROM leads l
        -- Get APPLICANT person data (one row per lead)
        LEFT JOIN LATERAL (
            SELECT (pd->>'personId')::uuid as person_id, (pd->>'leadPersonType') as lead_person_type, (pd->>'relationshipToPrimary') as relationship_to_primary 
            FROM jsonb_array_elements(l.person_data) AS pd
            WHERE (pd->>'leadPersonType') = 'APPLICANT'
            ORDER BY (pd->>'personId')::uuid -- Natural UUID sorting for better performance
        ) pd ON l.person_data IS NOT NULL
        LEFT JOIN person p ON pd.person_id = p.id
        -- Get first primary mobile number (if multiple primary numbers exist, show the first one)
        LEFT JOIN LATERAL (
            SELECT (mn->>'number') AS primary_mobile_number
            FROM jsonb_array_elements(p.mobile_numbers) AS mn
            WHERE (mn->>'isPrimary')::boolean = true
            ORDER BY (mn->>'number') -- Consistent ordering for deterministic results
            LIMIT 1
        ) mn ON p.mobile_numbers IS NOT NULL
        ORDER BY l.created_at DESC, pd.person_id
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

    /**
     * Comprehensive search for leads by phone number.
     * Searches across ALL mobile numbers (primary and non-primary) of ALL persons associated with the lead.
     * Returns multiple rows per lead if multiple persons have the same phone number.
     * Each row represents a person who has the matching phone number.
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
            matching_pd.person_id,
            matching_p.first_name,
            matching_p.middle_name,
            matching_p.last_name,
            matching_pd.mobile_number as mobile_number,
            matching_pd.lead_person_type,
            matching_pd.relationship_to_primary
        FROM leads l
        -- Get ALL persons who have the matching phone number (returns multiple rows if multiple persons match)
        JOIN LATERAL (
            SELECT 
                (pd->>'personId')::uuid as person_id, 
                (pd->>'leadPersonType') as lead_person_type, 
                (pd->>'relationshipToPrimary') as relationship_to_primary,
                (mn->>'number') as mobile_number
            FROM jsonb_array_elements(l.person_data) AS pd
            JOIN person p ON (pd->>'personId')::uuid = p.id
            CROSS JOIN LATERAL jsonb_array_elements(p.mobile_numbers) AS mn
            WHERE (mn->>'number') LIKE '%' || :phoneNumber || '%'
            ORDER BY (pd->>'leadPersonType') DESC, (pd->>'personId')::uuid -- APPLICANT first, then natural UUID sorting
        ) matching_pd ON l.person_data IS NOT NULL
        LEFT JOIN person matching_p ON matching_pd.person_id = matching_p.id
        ORDER BY l.created_at DESC, matching_pd.lead_person_type DESC, matching_pd.person_id
    """,
        nativeQuery = true
    )
    fun findSummaryDataByPhoneNumber(@Param("phoneNumber") phoneNumber: String): List<LeadSummaryDTO>
}
