package com.nivasafinance.features.applicant.repository

import com.nivasafinance.features.applicant.entity.Applicant
import com.nivasafinance.features.applicant.enum.ApplicantType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ApplicantRepository : JpaRepository<Applicant, UUID> {

    fun findByLeadId(leadId: UUID): List<Applicant>

    fun findByPersonId(personId: UUID): List<Applicant>

    fun findByLeadIdAndApplicantType(leadId: UUID, applicantType: ApplicantType): List<Applicant>
}
