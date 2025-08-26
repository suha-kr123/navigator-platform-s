package com.nivasafinance.features.wrapper.service

import com.nivasafinance.features.wrapper.dto.ApplicantWrapperRequest
import com.nivasafinance.features.wrapper.dto.ApplicantWrapperResponse
import java.util.UUID

interface ApplicantWrapperService {
    fun createApplicant(request: ApplicantWrapperRequest): ApplicantWrapperResponse
    fun getApplicant(applicantId: UUID): ApplicantWrapperResponse
}
