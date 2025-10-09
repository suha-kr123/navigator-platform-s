package com.nivasafinance.features.lead.repository

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

    @Query(
        value = """
        SELECT DISTINCT l.* FROM leads l 
        CROSS JOIN LATERAL jsonb_array_elements(l.person_data) AS pd
        JOIN person p ON (pd->>'personId')::uuid = p.id 
        WHERE (:search IS NULL OR :search = '' OR 
               LOWER(p.first_name) LIKE LOWER(CONCAT('%', :search, '%')) OR
               LOWER(p.last_name) LIKE LOWER(CONCAT('%', :search, '%')) OR
               LOWER(CONCAT(COALESCE(p.first_name, ''), ' ', COALESCE(p.last_name, ''))) LIKE LOWER(CONCAT('%', :search, '%')) OR
               p.mobile_numbers::text LIKE CONCAT('%', :search, '%'))
    """,
        nativeQuery = true
    )
    fun findByPersonNameOrPhoneNumber(@Param("search") search: String?, pageable: Pageable): Page<Lead>
}
