package com.nivasafinance.features.applicant.service

import com.nivasafinance.features.applicant.dto.ApplicantCreateRequest
import com.nivasafinance.features.applicant.dto.ApplicantUpdateRequest
import java.util.UUID

interface ApplicantWriteService {
    fun createApplicant(request: ApplicantCreateRequest): UUID
    fun updateApplicant(applicantId: UUID, request: ApplicantUpdateRequest)
    fun deleteApplicant(applicantId: UUID)
}
