package com.nivasafinance.features.advisor.repository

import com.nivasafinance.features.advisor.entity.Advisor
import org.javers.spring.annotation.JaversSpringDataAuditable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
@JaversSpringDataAuditable
interface AdvisorRepository : JpaRepository<Advisor, UUID> {

    @Query(
        "SELECT a.* FROM advisor a JOIN person p ON a.person_id = p.id " +
            "WHERE p.mobile_numbers @> jsonb_build_array(" +
            "jsonb_build_object('isPrimary', true, 'number', :primaryMobileNo))",
        nativeQuery = true
    )
    fun findByPrimaryMobileNo(primaryMobileNo: String): Advisor?

    fun findByPersonId(personId: UUID): Advisor?
}
