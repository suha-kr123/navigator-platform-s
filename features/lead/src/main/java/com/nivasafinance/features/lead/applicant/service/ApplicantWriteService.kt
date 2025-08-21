package com.nivasafinance.features.lead.applicant.service

import com.nivasafinance.features.lead.applicant.dto.ApplicantCreateRequest
import com.nivasafinance.features.lead.applicant.dto.ApplicantPatchRequest
import com.nivasafinance.features.lead.applicant.dto.ApplicantResponse
import data.Identifier
import java.util.UUID

interface ApplicantWriteService {
    fun createApplicant(
        personId: UUID,
        leadId: UUID,
        request: ApplicantCreateRequest
    ): ApplicantResponse

    fun patchApplicant(id: UUID, request: ApplicantPatchRequest): ApplicantResponse

    fun addIdentifier(applicantId: UUID, addIdentifier: Identifier)

    fun updateIdentifier(applicantId: UUID, identifierId: UUID, addIdentifier: Identifier)
}
