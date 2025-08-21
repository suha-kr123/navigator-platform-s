package com.nivasafinance.features.lead.applicant.service

import com.nivasafinance.features.lead.applicant.dto.ApplicantResponse
import data.IdentifierResponse
import java.util.UUID

interface ApplicantReadService {
    fun getApplicantById(id: UUID): ApplicantResponse
    fun getByIdentifierId(applicantId: UUID, identifierId: UUID): IdentifierResponse
}
