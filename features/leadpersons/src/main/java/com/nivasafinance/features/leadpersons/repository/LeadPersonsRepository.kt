package com.nivasafinance.features.leadpersons.repository

import com.nivasafinance.features.leadpersons.entity.LeadPersons
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface LeadPersonsRepository : JpaRepository<LeadPersons, UUID> {
    fun findByLeadId(leadId: UUID): List<LeadPersons>
    fun findByPersonId(personId: UUID): List<LeadPersons>
    fun existsByLeadIdAndPersonId(leadId: UUID, personId: UUID): Boolean
}
