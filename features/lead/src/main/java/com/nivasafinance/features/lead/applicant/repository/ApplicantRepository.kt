package com.nivasafinance.features.lead.applicant.repository

import com.nivasafinance.features.lead.applicant.entity.Applicant
import com.nivasafinance.features.lead.applicant.enum.ApplicantType
import org.javers.spring.annotation.JaversSpringDataAuditable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
@JaversSpringDataAuditable
interface ApplicantRepository : JpaRepository<Applicant, UUID> {

    fun findByLeadId(leadId: UUID): List<Applicant>

    fun findByLeadIdAndApplicantType(leadId: UUID, applicantType: ApplicantType): Applicant?

    fun findByPersonId(personId: UUID): List<Applicant>
}
