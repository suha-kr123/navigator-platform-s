package com.nivasafinance.features.applicant.service

import com.nivasafinance.features.applicant.dto.ApplicantCreateRequest
import com.nivasafinance.features.applicant.dto.ApplicantResponse
import com.nivasafinance.features.applicant.dto.ApplicantUpdateRequest
import java.util.UUID

interface ApplicantService {
    fun getApplicant(applicantId: UUID): ApplicantResponse
    fun getApplicantsByLeadId(leadId: UUID): List<ApplicantResponse>
    fun createApplicant(request: ApplicantCreateRequest): ApplicantResponse
    fun updateApplicant(applicantId: UUID, request: ApplicantUpdateRequest): ApplicantResponse
    fun deleteApplicant(applicantId: UUID)
}
