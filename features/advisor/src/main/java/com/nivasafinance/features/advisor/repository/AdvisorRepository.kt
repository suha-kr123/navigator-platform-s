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
            "WHERE p.mobile_number->>'primary' = :primaryMobileNo",
        nativeQuery = true
    )
    fun findByPrimaryMobileNo(primaryMobileNo: String): Advisor?

    fun findByPersonId(personId: UUID): Advisor?
}
