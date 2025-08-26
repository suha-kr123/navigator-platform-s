package com.nivasafinance.features.applicant.service

import com.nivasafinance.features.applicant.dto.ApplicantData
import java.util.UUID

interface ApplicantReadService {
    fun getApplicant(applicantId: UUID): ApplicantData
    fun getApplicantsByLeadId(leadId: UUID): List<ApplicantData>
}
