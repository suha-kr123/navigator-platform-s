package com.nivasafinance.features.person.repository

import com.nivasafinance.features.person.entity.EmploymentDetails
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface EmploymentDetailsRepository : JpaRepository<EmploymentDetails, UUID> {
    fun findByPersonId(personId: UUID): EmploymentDetails?
    fun existsByPersonId(personId: UUID): Boolean
}
