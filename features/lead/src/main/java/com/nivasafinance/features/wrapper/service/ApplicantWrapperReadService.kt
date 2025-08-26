package com.nivasafinance.features.wrapper.service

import com.nivasafinance.features.wrapper.dto.ApplicantWrapperResponse
import java.util.UUID

interface ApplicantWrapperReadService {
    fun getApplicant(applicantId: UUID): ApplicantWrapperResponse
}
